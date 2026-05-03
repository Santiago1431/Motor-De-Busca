package com.computational.search.api.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.openapitools.jackson.nullable.JsonNullable;
import java.io.Serializable;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Result
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-03T02:26:29.081622481-03:00[America/Sao_Paulo]")

public class Result  implements Serializable {
  private static final long serialVersionUID = 1L;

  @JsonProperty("title")
  private String title;

  @JsonProperty("url")
  private String url;

  @JsonProperty("abs")
  private String abs;

  @JsonProperty("formulas")
  @Valid
  private List<String> formulas = null;

  @JsonProperty("formulas_latex")
  @Valid
  private List<String> formulasLatex = null;

  public Result title(String title) {
    this.title = title;
    return this;
  }

  /**
   * Get title
   * @return title
  */
  @ApiModelProperty(value = "")


  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Result url(String url) {
    this.url = url;
    return this;
  }

  /**
   * Get url
   * @return url
  */
  @ApiModelProperty(value = "")


  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Result abs(String abs) {
    this.abs = abs;
    return this;
  }

  /**
   * Get abs
   * @return abs
  */
  @ApiModelProperty(value = "")


  public String getAbs() {
    return abs;
  }

  public void setAbs(String abs) {
    this.abs = abs;
  }

  public Result formulas(List<String> formulas) {
    this.formulas = formulas;
    return this;
  }

  public Result addFormulasItem(String formulasItem) {
    if (this.formulas == null) {
      this.formulas = new ArrayList<>();
    }
    this.formulas.add(formulasItem);
    return this;
  }

  /**
   * Get formulas
   * @return formulas
  */
  @ApiModelProperty(value = "")


  public List<String> getFormulas() {
    return formulas;
  }

  public void setFormulas(List<String> formulas) {
    this.formulas = formulas;
  }

  public Result formulasLatex(List<String> formulasLatex) {
    this.formulasLatex = formulasLatex;
    return this;
  }

  public Result addFormulasLatexItem(String formulasLatexItem) {
    if (this.formulasLatex == null) {
      this.formulasLatex = new ArrayList<>();
    }
    this.formulasLatex.add(formulasLatexItem);
    return this;
  }

  /**
   * Get formulasLatex
   * @return formulasLatex
  */
  @ApiModelProperty(value = "")


  public List<String> getFormulasLatex() {
    return formulasLatex;
  }

  public void setFormulasLatex(List<String> formulasLatex) {
    this.formulasLatex = formulasLatex;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Result result = (Result) o;
    return Objects.equals(this.title, result.title) &&
        Objects.equals(this.url, result.url) &&
        Objects.equals(this.abs, result.abs) &&
        Objects.equals(this.formulas, result.formulas) &&
        Objects.equals(this.formulasLatex, result.formulasLatex);
  }

  @Override
  public int hashCode() {
    return Objects.hash(title, url, abs, formulas, formulasLatex);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Result {\n");
    
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
    sb.append("    abs: ").append(toIndentedString(abs)).append("\n");
    sb.append("    formulas: ").append(toIndentedString(formulas)).append("\n");
    sb.append("    formulasLatex: ").append(toIndentedString(formulasLatex)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

