package org.jasig.portal.portlets.portletadmin.xmlsupport;

public interface ICPDOptionType {

	public String getBase();

	public void setBase(String base);

	public String getInput();

	public void setInput(String input);

	public String getDisplay();

	public void setDisplay(String display);

	public String getLength();

	public void setLength(String length);

	public String getMaxlength();

	public void setMaxlength(String maxlength);

	public ICPDOptionTypeRestriction getRestriction();

}
