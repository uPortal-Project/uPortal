package org.jasig.portal.layout.dlm.remoting.registry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.channel.IChannelParameter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("channel")
public class ChannelBean implements Serializable {
	
	@XStreamAlias("ID")
   	@XStreamAsAttribute
	private String id;
	
	@XStreamAlias("chanID")
   	@XStreamAsAttribute
	private int chanId;
	
   	@XStreamAsAttribute
	private String javaClass;
	
   	@XStreamAsAttribute
	private String description;
	
   	@XStreamAsAttribute
	private boolean editable;
	
   	@XStreamAsAttribute
	private String fname;

   	@XStreamAsAttribute
	private boolean hasAbout;
	
   	@XStreamAsAttribute
	private boolean hasHelp;
	
   	@XStreamAsAttribute
	private boolean isPortlet;
	
   	@XStreamAsAttribute
	private String locale;
	
   	@XStreamAsAttribute
	private String name;
	
   	@XStreamAsAttribute
	private boolean secure;
	
   	@XStreamAsAttribute
	private int timeout;
	
   	@XStreamAsAttribute
	private String state;
	
   	@XStreamAsAttribute
	private String title;
	
	@XStreamAlias("typeID")
   	@XStreamAsAttribute
	private int typeId;

   	@XStreamImplicit(itemFieldName="parameter")
   	private List<ChannelParameterBean> parameters;

	public ChannelBean(IChannelDefinition channel) {
		this.id = "chan" + channel.getId();
		this.chanId = channel.getId();
		this.javaClass = channel.getJavaClass();
		this.description = channel.getDescription();
		this.editable = channel.isEditable();
		this.fname = channel.getFName();
		this.hasAbout = channel.hasAbout();
		this.hasHelp = channel.hasHelp();
		this.isPortlet = channel.isPortlet();
		this.locale = channel.getLocale();
		this.name = channel.getName();
		this.secure = channel.isSecure();
		this.timeout = channel.getTimeout();
		this.state = channel.getLifecycleState().toString();
		this.title = channel.getTitle();
		this.typeId = channel.getType().getId();
		this.parameters = new ArrayList<ChannelParameterBean>();
		for(IChannelParameter param : channel.getParameters()) {
			ChannelParameterBean parameter = new ChannelParameterBean(param);
			this.addParameter(parameter);
		}
	}
	
	public void addParameter(ChannelParameterBean parameter) {
		this.parameters.add(parameter);
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getChanId() {
		return this.chanId;
	}

	public void setChanId(int chanId) {
		this.chanId = chanId;
	}

	public String getJavaClass() {
		return this.javaClass;
	}

	public void setJavaClass(String javaClass) {
		this.javaClass = javaClass;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isEditable() {
		return this.editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public String getFname() {
		return this.fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	public boolean isHasAbout() {
		return this.hasAbout;
	}

	public void setHasAbout(boolean hasAbout) {
		this.hasAbout = hasAbout;
	}

	public boolean isHasHelp() {
		return this.hasHelp;
	}

	public void setHasHelp(boolean hasHelp) {
		this.hasHelp = hasHelp;
	}

	public boolean isPortlet() {
		return this.isPortlet;
	}

	public void setPortlet(boolean isPortlet) {
		this.isPortlet = isPortlet;
	}

	public String getLocale() {
		return this.locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSecure() {
		return this.secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public int getTimeout() {
		return this.timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getState() {
		return this.state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getTypeId() {
		return this.typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

}
