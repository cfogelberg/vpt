package uk.co.bristlecone.voltdb.wrapgen.builder;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import uk.co.bristlecone.voltdb.wrapgen.source.RunParameter;
import uk.co.bristlecone.voltdb.wrapgen.source.ProcReturnType;
import uk.co.bristlecone.voltdb.wrapgen.source.SourceFile;

/**
 * An immutable class representing the metadata needed to build a wrapgen runner
 * 
 * @author christo
 */
public class ProcData {
  private String name;
  private List<RunParameter> parameters;
  private ProcReturnType returnType;

  private ProcData(String name, List<RunParameter> parameters, ProcReturnType returnType) {
    this.name = name;
    this.parameters = parameters;
    this.returnType = returnType;
  }

  private ProcData(SourceFile source) {
    this.name = source.voltProcedureName();
    this.parameters = source.runMethodParameters();
    this.returnType = source.runMethodReturnType();
  }

  /**
   * @return the (unqualified) name of the stored procedure class
   */
  public String name() {
    return name;
  }

  /**
   * @return the parameters to the stored procedure's <code>run</code> method
   */
  public List<RunParameter> parameters() {
    return parameters;
  }

  /**
   * @return the stored procedure's return type
   */
  public ProcReturnType returnType() {
    return returnType;
  }

  /**
   * Allows clear, type-safe building of a ProcData
   * 
   * @author christo
   */
  public static class Builder {
    private String name;
    private List<RunParameter> parameters;
    private ProcReturnType returnType;

    public Builder() {
      // intentionally left blank
    }

    public Builder setName(String name) {
      checkArgument(name != null, "name must not be null");
      checkArgument(!name.equals(""), "name must not be empty String");
      this.name = name;
      return this;
    }

    public Builder setParameters(List<RunParameter> parameters) {
      checkArgument(parameters != null, "parameters must not be null");
      this.parameters = parameters;
      return this;
    }

    public Builder setReturnType(ProcReturnType returnType) {
      checkArgument(name != null, "returnType must not be null");
      this.returnType = returnType;
      return this;
    }

    public ProcData build() {
      return new ProcData(name, parameters, returnType);
    }
  }
}