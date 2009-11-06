/**
 * 
 */
package org.jasig.portal.web.skin;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.jasig.org/uportal/web/skin}css" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.jasig.org/uportal/web/skin}js" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "css",
    "js"
})
@XmlRootElement(name = "resources", namespace = "http://www.jasig.org/uportal/web/skin")
public class Resources {

    private List<Css> css;
    private List<Js> js;

    /**
     * Gets the value of the css property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the css property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCss().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Css }
     * 
     * 
     */
    public List<Css> getCss() {
        if (css == null) {
            css = new ArrayList<Css>();
        }
        return this.css;
    }

    /**
     * Gets the value of the js property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the js property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getJs().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<Js> getJs() {
        if (js == null) {
            js = new ArrayList<Js>();
        }
        return this.js;
    }

}
