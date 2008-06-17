/* Copyright 2001, 2002, 2005 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.RDBMServices;

/**
 * LayoutStructure represents a channel or folder in a layout.
 * @version $Revision$ $Date$
 * @since uPortal 2.5 - before 2.5 this class existed as a public inner class of RDBMUserLayoutStore.
 */
public final class LayoutStructure {

    private static final Log LOG = LogFactory.getLog(LayoutStructure.class);

    /**
     * The ID of this LayoutStructure.
     */
    private final int structId;

    /**
     * The ID of the LayoutStructure that is the next sibling of this LayoutStructure.
     */
    private final int nextId;

    /**
     * The ID of the LayoutStructure that is the child of this LayoutStructure.
     */
    private final int childId;

    /**
     * The ID of any channel that this LayoutStructure instance is representing.
     * Zero if this LayoutStructure instance does not represent a channel.
     */
    private final int chanId;

    /**
     * When this LayoutStructure represents a folder, the name of that folder.
     * Null otherwise.
     */
    private String name;

    /**
     * When this LayoutStructure represents a folder, the type of that folder.
     * Null otherwise.
     */
    private String type;

    /**
     * True if this LayoutStructure is hidden, false otherwise.
     */
    private boolean hidden;

    /**
     * True if this LayoutStructure cannot be removed, false otherwise.
     */
    private boolean unremovable;

    /**
     * True if this LayoutStructure cannot be changed, false otherwise.
     */
    private boolean immutable;

    /**
     * A List of StructureParameter instances representing parameters
     * to this LayoutStructure. 
     * 
     * Prior to uPortal 2.5, this field was null when there were no parameters.  Now
     * this field is never null and no parameters are repreesented by an empty list.
     */
    private final List parameters = new ArrayList();

    private String locale;

    /**
     * Instantiate a new LayoutStructure with the given configuration.
     * @param structId the id of this LayoutStructure
     * @param nextId the id of the next sibling of this LayoutStructure
     * @param childId the id of the first child of this LayoutStructure
     * @param chanId the id of the channel represented by this LayoutStructure, or zero if we do not represent a channel
     * @param hidden "T" or "Y" if this LayoutStructure is hidden
     * @param unremovable "T" or "Y" if this LayoutStructure is unremovable
     * @param immutable "T" or "Y" if this LayoutStructure is unchangeable
     */
    public LayoutStructure(int structId, int nextId, int childId, int chanId,
            String hidden, String unremovable, String immutable) {
        this.nextId = nextId;
        this.childId = childId;
        this.chanId = chanId;
        this.structId = structId;
        this.hidden = RDBMServices.dbFlag(hidden);
        this.immutable = RDBMServices.dbFlag(immutable);
        this.unremovable = RDBMServices.dbFlag(unremovable);

        if (LOG.isTraceEnabled()) {
            LOG.trace("Instantiated new " + this);
        }
    }

    /**
     * Instantiate a new LayoutStructure with the given configuration.
     * @param structId the id of this LayoutStructure
     * @param nextId the id of the next sibling of this LayoutStructure
     * @param childId the id of the first child of this LayoutStructure
     * @param chanId the id of the channel represented by this LayoutStructure, or zero if we do not represent a channel
     * @param hidden "T" or "Y" if this LayoutStructure is hidden
     * @param unremovable "T" or "Y" if this LayoutStructure is unremovable
     * @param immutable "T" or "Y" if this LayoutStructure is unchangeable
     * @param locale the locale of this LayoutStructure
     */
    public LayoutStructure(int structId, int nextId, int childId, int chanId,
            String hidden, String unremovable, String immutable, String locale) {
        this.nextId = nextId;
        this.childId = childId;
        this.chanId = chanId;
        this.structId = structId;
        this.hidden = RDBMServices.dbFlag(hidden);
        this.immutable = RDBMServices.dbFlag(immutable);
        this.unremovable = RDBMServices.dbFlag(unremovable);
        this.locale = locale; // for i18n by Shoji

        if (LOG.isTraceEnabled()) {
            LOG.trace("Instantiated new " + this);
        }
    }

    /**
     * Add information about the folder represented by this LayoutStructure.
     * @param folderName the name of the folder
     * @param folderType the type of the folder
     */
    public void addFolderData(String folderName, String folderType) {
        this.name = folderName;
        this.type = folderType;
    }

    /**
     * Returns true if this LayoutStructure represents a channel, false otherwise.
     * Otherwise is the case where this LayoutStructure represents a folder.
     * @return true if a channel, false if a folder.
     */
    public boolean isChannel() {
        return this.chanId != 0;
    }

    
    /**
     * Add a parameter to this LayoutStructure.
     * @param paramName the name of the parameter
     * @param paramValue the value of the parameter
     */
    public void addParameter(String paramName, String paramValue) {
        this.parameters.add(new StructureParameter(paramName, paramValue));
    }

    /**
     * Get the id of the next LayoutStructure or zero if there is no next LayoutStructure.
     * @return 0 or the id of the next layout structure.
     */
    public int getNextId() {
        return this.nextId;
    }

    /**
     * Get the id of the child of this LayoutStructure, or zero if we do not have
     * a child.
     * @return 0 or the id of our child.
     */
    public int getChildId() {
        return this.childId;
    }

    /**
     * Get the id of the channel represented by this LayoutStructure instance,
     * or zero if we do not represent a channel.
     * @return 0 or the id of the channel
     */
    public int getChanId() {
        return this.chanId;
    }

    /**
     * Get the id of this LayoutStructure.
     * 
     * @return the id of this LayoutStructure.
     */
    public int getStructId() {
        return this.structId;
    }

    /**
     * Return true if this LayoutStructure is hidden, false otherwise.
     * 
     * @return true if this LayoutStructure is hidden, false otherwise.
     */
    public boolean isHidden() {
        return this.hidden;
    }

    /**
     * Returns true if this LayoutStructure is immutable, false otherwise.
     * 
     * @return false if this LayoutStructure can be changed, true otherwise.
     */
    public boolean isImmutable() {
        return this.immutable;
    }

    /**
     * Get the locale of this LayoutStructure.
     * 
     * @return the locale of this LayoutStructure.
     */
    public String getLocale() {
        return this.locale;
    }

    /**
     * Get the name of the folder that this LayoutStructure represents, or null
     * if this LayoutStructure does not represent a folder.
     * 
     * @return the name of this LayoutStructure.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get a List of StructureParameter instances representing parameters of
     * this LayoutStructure instance.
     * 
     * Prior to uPortal 2.5, this method would return null when there were no
     * parameters.  Now we return an empty list when there are no parameters.
     * This simplifies consumers of this class, who can simply blithely iterate over
     * the empty list and no longer have to check for null.
     * 
     * @return a List of StructureParameter instances.
     */
    public List getParameters() {
        return this.parameters;
    }

    /**
     * Get the String representing the type of the folder that this LayoutStructure
     * represents, or null if this LayoutStructure does not represent a folder.
     * Different layout management approaches may define differing types of
     * folders for their own purposes. The core types typically used by all
     * are: header, footer, and regular. The value returned is the value found
     * in the up_layout_struct table's type column. For instances of 
     * LayoutStructure that represent a channel this method will return a 
     * value of null.
     * 
     * @return a String representing the type of this layout structure.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Return true if this structure is unremovable, false otherwise.
     * 
     * @return false if this structure can be removed, true otherwise.
     */
    public boolean isUnremovable() {
        return this.unremovable;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("LayoutStructure:");
        sb.append(" structId = ").append(this.structId);
        sb.append(" nextId = ").append(this.nextId);
        sb.append(" childId = ").append(this.childId);
        sb.append(" chanId = ").append(this.chanId);
        sb.append(" name = [").append(this.name).append("]");
        sb.append(" hidden = ").append(this.hidden);
        sb.append(" unremovable = ").append(this.unremovable);
        sb.append(" immutable = ").append(this.immutable);
        sb.append(" parameters = [").append(this.parameters);
        sb.append(" locale = [").append(this.locale).append("]");

        return sb.toString();
    }
}