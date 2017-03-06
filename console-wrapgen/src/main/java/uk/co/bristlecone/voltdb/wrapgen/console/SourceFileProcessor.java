package uk.co.bristlecone.voltdb.wrapgen.console;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.bristlecone.voltdb.wrapgen.builder.ProcData;
import uk.co.bristlecone.voltdb.wrapgen.builder.RunnerBuilder;
import uk.co.bristlecone.voltdb.wrapgen.builder.VoltRunnerJavaSource;
import uk.co.bristlecone.voltdb.wrapgen.console.SourceFileResult.Result;
import uk.co.bristlecone.voltdb.wrapgen.console.impl.PackageNamer;
import uk.co.bristlecone.voltdb.wrapgen.source.SourceFile;

/**
 * @see {@link SourceFileProcessor#process} for the SRP of this class.
 *
 * @author christo
 */
public class SourceFileProcessor {
  public static final Logger LOGGER = LoggerFactory.getLogger(SourceFileProcessor.class);

  private final Configuration config;
  private final PackageNamer packageNamer;

  public SourceFileProcessor(final Configuration config) {
    this.config = config;
    packageNamer = new PackageNamer(config.packageBase(), config.regexSuffix());
  }

  /**
   * Processes a {@link SourceFile}, describing the processing result via the returned {@link SourceFileResult}.
   *
   * If <code>sf</code> is a valid VoltDb stored procedure a {@link RunnerBuilder} is created and the resulting runner
   * class written to disk. If it is not a stored procedure or not a valid stored procedure this is also reflected in
   * the return value.
   *
   * @param sf The <code>SourceFile</code> to be processed.
   * @return The result of processing <code>sf</code>
   */
  public SourceFileResult process(final SourceFile sf) {
    if(!sf.isIntendedVoltProcedure()) {
      return SourceFileResult.of(sf.identifier(), Result.NOT_A_PROC);
    } else if(sf.isIntendedVoltProcedure() && !sf.isValidVoltProcedure()) {
      return SourceFileResult.of(sf.identifier(), Result.INVALID_PROC);
    } else { // looks like a valid proc
      return doValidProc(sf);
    }
  }

  private SourceFileResult doValidProc(final SourceFile sf) {
    final ProcData pd = new ProcData.Builder().setClassJavaDoc(sf.classJavaDoc())
        .setName(sf.voltProcedureName())
        .setPackageName(sf.packageName())
        .setParameters(sf.runMethodParameters())
        .setReturnType(sf.runMethodReturnType())
        .build();
    final VoltRunnerJavaSource vrjs = new RunnerBuilder(pd, packageNamer::getPackage, Function.identity()).build();
    final Path dest = getDestinationPath(vrjs.fullyQualifiedClassFilePath());
    if(Files.exists(dest)) {
      return doValidProcOverwrite(dest, vrjs, sf);
    } else {
      return doValidProcWrite(dest, vrjs, sf);
    }
  }

  private Path getDestinationPath(final String procChildPath) {
    return config.destDir()
        .resolve(procChildPath);
  }

  private SourceFileResult doValidProcOverwrite(final Path dest, final VoltRunnerJavaSource vrjs, final SourceFile sf) {
    try {
      Files.move(dest, getBackupPath(vrjs.fullyQualifiedClassFilePath()), StandardCopyOption.REPLACE_EXISTING,
          StandardCopyOption.COPY_ATTRIBUTES);
      Files.write(dest, vrjs.source()
          .getBytes());
      return SourceFileResult.of(sf.identifier(), Result.RUNNER_OVERWRITTEN);
    } catch (final IOException e) {
      LOGGER.error("Error backing up and replacing class with same path as " + sf.identifier(), e);
      return SourceFileResult.of(sf.identifier(), Result.RUNNER_WRITE_ERROR);
    }
  }

  private Path getBackupPath(final String procChildPath) {
    final String datetime = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    return config.destDir()
        .resolve(String.format("%s.%s", procChildPath, datetime));
  }

  private SourceFileResult doValidProcWrite(final Path dest, final VoltRunnerJavaSource vrjs, final SourceFile sf) {
    try {
      Files.write(Paths.get(vrjs.fullyQualifiedClassFilePath()), vrjs.source()
          .getBytes());
      return SourceFileResult.of(sf.identifier(), Result.RUNNER_WRITTEN);
    } catch (final IOException e) {
      LOGGER.error("Error writing runner to " + dest, e);
      return SourceFileResult.of(sf.identifier(), Result.RUNNER_WRITE_ERROR);
    }
  }
}
