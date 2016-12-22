package uk.co.bristlecone.voltdb.wrapgen.source;

import java.util.List;

import uk.co.bristlecone.voltdb.wrapgen.WrapgenRuntimeException;
import uk.co.bristlecone.voltdb.wrapgen.runner.VoltRunner;

/**
 * Represents a Java source file and provides accessor methods for extracting metadata needed to build a
 * {@link VoltRunner}. These accessor methods may throw a {@link WrapgenRuntimeException} if the associated Java source
 * does not contain a valid VoltDB stored procedure, so the {@link SourceFile#isValidVoltProcedure} method should be
 * used to guard against this.
 * 
 * @author christo
 */
public interface SourceFile {
  /**
   * @return true iff this SourceFile contains a valid VoltDB stored procedure
   */
  boolean isValidVoltProcedure();

  /**
   * @return the name of the VoltDB stored procedure in this source file; does not return an Optional but throws a
   *         {@link WrapgenRuntimeException} if the associated Java source does not contain a valid VoltDB stored
   *         procedure
   */
  String voltProcedureName();

  /**
   * @return the parameters to the VoltDB stored procedure's run method; does not return an Optional but throws a
   *         {@link WrapgenRuntimeException} if the associated Java source does not contain a valid VoltDB stored
   *         procedure
   */
  List<RunParameter> runMethodParameters();

  /**
   * @return the return type of the VoltDB stored procedure in this source file; does not return an Optional but throws
   *         a {@link WrapgenRuntimeException} if the associated Java source does not contain a valid VoltDB stored
   *         procedure
   */
  ProcReturnType runMethodReturnType();

  /**
   * @return the name of the package the VoltDB stored procedure is in; returns the empty String ("") for the default
   *         package; does not return an Optional but throws a {@link WrapgenRuntimeException} if the associated Java
   *         source does not contain a valid VoltDB stored procedure
   */
  String packageName();
}