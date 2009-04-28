package org.jasig.portal.portlets.portletadmin.xmlsupport;

import java.io.Serializable;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("step")
public class CPDStep implements Serializable {

	@XStreamAlias("ID")
	private String id;
	
	private String name;

	private String description;
   	
	@XStreamImplicit(itemFieldName="parameter")
   	private List<CPDParameter> parameters;
	
	@XStreamImplicit(itemFieldName="arbitrary-parameters")
	private List<CPDArbitraryParameter> arbitraryParameters;
	
	public CPDStep() { }
	
	public CPDStep(String id, String name, String description, List<CPDParameter> parameters, List<CPDArbitraryParameter> arbitraryParameters) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.parameters = parameters;
		this.arbitraryParameters = arbitraryParameters;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<CPDParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<CPDParameter> parameters) {
		this.parameters = parameters;
	}

	public List<CPDArbitraryParameter> getArbitraryParameters() {
		return arbitraryParameters;
	}

	public void setArbitraryParameters(
			List<CPDArbitraryParameter> arbitraryParameters) {
		this.arbitraryParameters = arbitraryParameters;
	}

	
}
