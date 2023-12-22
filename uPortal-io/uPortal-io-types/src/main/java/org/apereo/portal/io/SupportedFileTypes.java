/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.io;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apereo.portal.layout.IUserLayoutStore;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;

public enum SupportedFileTypes {
    LAYOUT("layout", "@username") {
        @Override
        protected boolean appliesTo(Element e, IUserLayoutStore rdbmdls) {
            boolean result = false; // default
            boolean isLayout = e.getName().equals(rootElementNodeName);
            if (isLayout) {
                String username = ((Node) documentNameXPath.evaluate(e)).getText();
                result = !rdbmdls.isFragmentOwner(username);
            }
            return result;
        }
    },
    FRAGMENT_LAYOUT("layout", "@username", "fragment-layout") {
        @Override
        protected boolean appliesTo(Element e, IUserLayoutStore rdbmdls) {
            boolean result = false; // default
            boolean isLayout = e.getName().equals(rootElementNodeName);
            if (isLayout) {
                String username = ((Node) documentNameXPath.evaluate(e)).getText();
                result = rdbmdls.isFragmentOwner(username);
            }
            return result;
        }
    },
    PROFILE("profile", "@username") {
        // Instance Members
        private final XPath profileFNameXPath = fac.createXPath("fname");

        @Override
        public String getSafeFileNameWithExtension(Element e) {
            String namePart = ((Node) documentNameXPath.evaluate(e)).getText();
            if (namePart == null) {
                String msg =
                        "The XPath expression '"
                                + documentNameXPath.getText()
                                + "' didn't match any text in the specified element:  "
                                + e.getName();
                throw new RuntimeException(msg);
            }
            String fNamePart = ((Node) profileFNameXPath.evaluate(e)).getText();
            if (fNamePart == null) {
                String msg =
                        "The XPath expression '"
                                + profileFNameXPath.getText()
                                + "' didn't match any text in the specified element:  "
                                + e.getName();
                throw new RuntimeException(msg);
            }
            StringBuilder result = new StringBuilder();
            result.append(makeSafe(namePart))
                    .append("-")
                    .append(makeSafe(fNamePart))
                    .append(".")
                    .append(fileExtension);
            return result.toString();
        }
    },
    PERMISSION("permission", null) {
        @Override
        protected boolean appliesTo(Element e, IUserLayoutStore rdbmdls) {
            return false;
        }

        @Override
        public String getSafeFileNameWithExtension(Element e) {
            throw new UnsupportedOperationException("Not yet supported.");
        }
    },
    PERMISSION_SET("permission-set", null) {
        // Instance Members
        private final XPath principalXPath = fac.createXPath("principal/*");
        private final XPath activityXPath = fac.createXPath("activity");
        private final XPath ownerXPath = fac.createXPath("owner");

        @Override
        public String getSafeFileNameWithExtension(Element e) {
            String principalPart = ((Node) principalXPath.evaluate(e)).getText();
            if (principalPart == null) {
                String msg =
                        "The XPath expression '"
                                + principalXPath.getText()
                                + "' didn't match any text in the specified element:  "
                                + e.getName();
                throw new RuntimeException(msg);
            }
            String activityPart = ((Node) activityXPath.evaluate(e)).getText();
            if (activityPart == null) {
                String msg =
                        "The XPath expression '"
                                + activityXPath.getText()
                                + "' didn't match any text in the specified element:  "
                                + e.getName();
                throw new RuntimeException(msg);
            }
            String ownerPart = ((Node) ownerXPath.evaluate(e)).getText();
            if (ownerPart == null) {
                String msg =
                        "The XPath expression '"
                                + ownerXPath.getText()
                                + "' didn't match any text in the specified element:  "
                                + e.getName();
                throw new RuntimeException(msg);
            }
            StringBuilder result = new StringBuilder();
            result.append(makeSafe(principalPart))
                    .append("__")
                    .append(makeSafe(activityPart))
                    .append("__")
                    .append(makeSafe(ownerPart))
                    .append(".")
                    .append(fileExtension);
            return result.toString();
        }
    },
    MEMBERSHIP("membership", null) {
        @Override
        protected boolean appliesTo(Element e, IUserLayoutStore rdbmdls) {
            return false;
        }

        @Override
        public String getSafeFileNameWithExtension(Element e) {
            throw new UnsupportedOperationException("Not yet supported.");
        }
    },
    CHANNEL("channel-definition", "fname", "channel"),
    CHANNEL_TYPE("channel-type", "name"),
    GROUP("group", "name") {
        private final XPath childrenXPath = fac.createXPath("children");

        @Override
        protected boolean appliesTo(Element e, IUserLayoutStore rdbmdls) {
            boolean result = false; // default
            boolean isGroup = e.getName().equals(rootElementNodeName);
            if (isGroup) {
                Object childrenElement = childrenXPath.evaluate(e);
                if (childrenElement != null && childrenElement instanceof List<?>) {
                    List<?> nodes = (List<?>) childrenElement;
                    // evaluate() returns an empty list if there are no matches
                    result = nodes.isEmpty();
                }
            }
            return result;
        }
    },
    GROUP_MEMBERSHIP("group", "name", "group_membership") {
        private final XPath childrenXPath = fac.createXPath("children");

        @Override
        protected boolean appliesTo(Element e, IUserLayoutStore rdbmdls) {
            boolean result = false; // default
            boolean isGroup = e.getName().equals(rootElementNodeName);
            if (isGroup) {
                Object childrenElement = childrenXPath.evaluate(e);
                if (childrenElement != null && childrenElement instanceof Node) {
                    result = true;
                }
            }
            return result;
        }
    },
    USER("user", "@username") {
        private final XPath templateUserXPath = fac.createXPath("default-user");

        @Override
        protected boolean appliesTo(Element e, IUserLayoutStore rdbmdls) {
            boolean result = false; // default
            boolean isUser = e.getName().equals(rootElementNodeName);
            if (isUser) {
                Object defaultUserElement = templateUserXPath.evaluate(e);
                if (defaultUserElement != null && defaultUserElement instanceof Node) {
                    result = true;
                }
            }
            return result;
        }
    },
    THEME("theme", "name"),
    STRUCTURE("structure", "name"),
    ENTITY_TYPE("entity-type", "name"),
    FRAGMENT_DEFINITION("fragment-definition", "*/@name");

    // Reserved names on Windows (see http://en.wikipedia.org/wiki/Filename)
    private static final Pattern[] WINDOWS_INVALID_PATTERNS =
            new Pattern[] {
                Pattern.compile("AUX"),
                Pattern.compile("CLOCK\\$"),
                Pattern.compile("COM\\d*"),
                Pattern.compile("CON"),
                Pattern.compile("LPT\\d*"),
                Pattern.compile("NUL"),
                Pattern.compile("PRN")
            };

    private static Map<Pattern, String> REPLACEMENT_PAIRS;

    static {
        final Map<Pattern, String> pairs = new LinkedHashMap<Pattern, String>();
        pairs.put(Pattern.compile("/|\\\\"), ".");
        pairs.put(Pattern.compile("[~`@\\|\\s#$\\*]"), "_");
        REPLACEMENT_PAIRS = Collections.unmodifiableMap(pairs);
    }

    // Instance Members.
    protected final String rootElementNodeName;
    protected final XPath documentNameXPath;
    protected final String fileExtension;
    protected final DocumentFactory fac = new DocumentFactory();

    /*
     * Public API.
     */

    public static SupportedFileTypes getApplicableFileType(Element e, IUserLayoutStore rdbmdls) {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e' [Element] cannot be null";
            throw new IllegalArgumentException(msg);
        }
        if (rdbmdls == null) {
            String msg = "Argument 'rdbmdls' [IUserLayoutStore] cannot be null";
            throw new IllegalArgumentException(msg);
        }

        SupportedFileTypes result = null;
        for (SupportedFileTypes y : SupportedFileTypes.values()) {
            if (y.appliesTo(e, rdbmdls)) {
                result = y;
                break;
            }
        }
        if (result == null) {
            String msg =
                    "SupportedFileTypes instance not found for the specified element:  "
                            + e.getName();
            throw new RuntimeException(msg);
        }
        return result;
    }

    public String getSafeFileNameWithExtension(Element e) {
        String namePart = ((Node) documentNameXPath.evaluate(e)).getText();
        if (namePart == null) {
            String msg =
                    "The XPath expression '"
                            + documentNameXPath.getText()
                            + "' didn't match any text in the specified element:  "
                            + e.getName();
            throw new RuntimeException(msg);
        }
        return makeSafe(namePart) + "." + fileExtension;
    }

    /*
     * Non-Public Stuff.
     */

    SupportedFileTypes(String rootElementNodeName, String documentNameExpression) {
        this(rootElementNodeName, documentNameExpression, rootElementNodeName);
    }

    SupportedFileTypes(
            String rootElementNodeName, String documentNameExpression, String fileExtension) {
        this.rootElementNodeName = rootElementNodeName;
        this.documentNameXPath =
                documentNameExpression != null ? fac.createXPath(documentNameExpression) : null;
        this.fileExtension = fileExtension;
    }

    protected boolean appliesTo(Element e, IUserLayoutStore rdbmdls) {
        return e.getName().equals(rootElementNodeName);
    }

    protected final String makeSafe(String name) {
        // Replace invalid characters
        for (final Map.Entry<Pattern, String> pair : REPLACEMENT_PAIRS.entrySet()) {
            final Pattern pattern = pair.getKey();
            final Matcher matcher = pattern.matcher(name);
            name = matcher.replaceAll(pair.getValue());
        }
        // Make sure the name doesn't violate a Windows reserved word...
        final String upperCaseName = name.toUpperCase();
        for (Pattern pattern : WINDOWS_INVALID_PATTERNS) {
            if (pattern.matcher(upperCaseName).matches()) {
                name = "uP-" + name;
                break;
            }
        }
        return name;
    }
}
