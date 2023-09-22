/*******************************************************************************
 * Copyright (c) 2018, 2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.ws.feature.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

import com.ibm.ws.feature.utils.FeatureConstants;
import com.ibm.ws.feature.utils.FeatureFiles;
import com.ibm.ws.feature.utils.FeatureInfo;
import com.ibm.ws.feature.utils.FeatureRepo;

import aQute.bnd.header.Attrs;

public class FeatureTest {
    private static final String REPOSITORY_ROOT = "./visibility";

    private static final FeatureRepo repository = readRepository(REPOSITORY_ROOT);

    public static FeatureRepo getRepository() {
        return repository;
    }

    public static void forEach(Consumer<? super FeatureInfo> consumer) {
        getRepository().forEach(consumer);
    }

    public static FeatureInfo getFeature(String feature) {
        return getRepository().getFeature(feature);
    }

    public static int getNumFeatures() {
        return getRepository().getNumFeatures();
    }

    public Map<String, String> getBaseVisibilities() {
        return getRepository().getBaseVisibilities();
    }

    private static FeatureRepo readRepository(String repositoryRoot) {
        try {
            return FeatureRepo.readFeatures(new File(repositoryRoot));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read features [ " + repositoryRoot + " ]", e);
        }
    }

    //

    @Test
    public void testFileNames() {
        StringBuilder builder = new StringBuilder();

        forEach((FeatureInfo featureInfo) -> {
            String featureName = featureInfo.getName();
            String featureFileName = featureInfo.getFeatureFileName();

            if (!featureName.equals(featureFileName)) {
                appendLine(builder,
                           "  Feature [ ", featureName, " ] in [ ", featureFileName + " ]");
            }
        });

        String title = "Feature file name errors:";
        maybeFail(builder, title);
    }

    @Test
    public void testVisibilityFile() {
        StringBuilder builder = new StringBuilder();

        FeatureFiles featureFiles = getRepository().getFeatureFiles();

        forEach((FeatureInfo featureInfo) -> {
            File featureFile = featureInfo.getFeatureFile();

            String visibility = featureInfo.getVisibility();

            String visibilityCategory;
            if (featureInfo.isAutoFeature()) {
                if (!FeatureConstants.VISIBILITY_PRIVATE.equals(visibility)) {
                    // This is an error; caught elsewhere.
                    return;
                }
                visibilityCategory = FeatureConstants.VISIBILITY_AUTO;
            } else {
                visibilityCategory = visibility;
            }

            Set<File> category = featureFiles.getCategory(visibilityCategory);

            if (!category.contains(featureFile)) {
                File actualCategory = featureFiles.getActualCategory(featureInfo.getFeatureFile());

                appendLine(builder,
                           "  [ ", featureInfo.getName(), " : ", featureInfo.getVisibility(), " ]",
                           " missing from [ ", visibilityCategory, " ]",
                           " found in [ ", actualCategory.getName(), " ]");
            }
        });

        String title = "Feature visibility placement errors:";
        maybeFail(builder, title);
    }

    @Test
    public void testBaseVisibility() {
        StringBuilder builder = new StringBuilder();

        int numFeatures = getNumFeatures();

        Map<String, String> baseFeatures = new HashMap<>(numFeatures);
        Map<String, String> baseVisibilities = new HashMap<>(numFeatures);

        forEach((FeatureInfo featureInfo) -> {
            if (featureInfo.isAutoFeature() || (featureInfo.getVersion() == null)) {
                return;
            }

            String name = featureInfo.getName();
            String baseName = featureInfo.getBaseName();
            String visibility = featureInfo.getVisibility();

            String priorVisibility = baseVisibilities.get(baseName);
            if (priorVisibility == null) {
                baseFeatures.put(baseName, name);
                baseVisibilities.put(baseName, priorVisibility);

            } else {
                String priorFeature = baseFeatures.get(baseName);

                appendLine(builder,
                           " Base name [ ", baseName, " ] conflict:",
                           " Feature [ ", name, " : ", visibility, " ]: ",
                           " Prior feature [ ", priorFeature, " ] [ ", priorVisibility, " ]");
            }
        });

        String title = "Feature base visibility errors:";
        maybeFail(builder, title);
    }

    /**
     * Perform visibility related checks:
     *
     * <ul>
     * <li>The visibility must be one of the four allowed values: auto, private, protected, or public.</li>
     * <li>Auto-features must be private.</li>
     * <li>Public features must have a short name and must be in a sub-directory that has that name.</li>
     * <li>Non-public admin-center features must have a short name, and must be in a sub-directory
     * that has that name.</li>
     * <li>Non-public non-admin-center features must not have a short name, and must be directly in
     * the visibility subdirectory.</li>
     * <li>Non-public features must not disable all features on conflict.</li>
     * <li>Non-public features must not set also-known-as.</li>
     * </ul>
     */
    @Test
    public void testVisibilityAttributes() {
        StringBuilder builder = new StringBuilder();

        forEach((FeatureInfo featureInfo) -> {
            String featureName = featureInfo.getName();
            String shortName = featureInfo.getShortName();

            // Features that start with "com.ibm.websphere.appserver.adminCenter.tool"
            // must be stored in their own directory and must have an IBM-ShortName.

            boolean isAdminCenter = featureName.startsWith("com.ibm.websphere.appserver.adminCenter.tool");

            String visibility = featureInfo.getVisibility();

            boolean isPublic = false;
            boolean isPrivate = false;

            if (!visibility.equals(FeatureConstants.VISIBILITY_AUTO) &&
                !(isPrivate = visibility.equals(FeatureConstants.VISIBILITY_PRIVATE)) &&
                !visibility.equals(FeatureConstants.VISIBILITY_PROTECTED) &&
                !(isPublic = visibility.equals(FeatureConstants.VISIBILITY_PUBLIC))) {

                appendLine(builder,
                           "Feature [ ", featureName, " ]",
                           " has unknown visibility [ ", visibility, " ]");
                return;
            }

            String fileVisibility;
            if (featureInfo.isAutoFeature()) {
                if (!isPrivate) {
                    appendLine(builder,
                               "Auto-feature [ ", featureName, " ] is [ ", visibility, " ]",
                               " but should be [ ", FeatureConstants.VISIBILITY_PRIVATE, " ]");
                }
                fileVisibility = FeatureConstants.VISIBILITY_AUTO;
            } else {
                fileVisibility = visibility;
            }

            boolean inSubDir;
            if (isPublic || isAdminCenter) {
                inSubDir = true;

                if (shortName == null) {
                    appendLine(builder,
                               "Public or adminCenter feature [ ", featureName, " ]",
                               " has no short name");
                }
            } else {
                inSubDir = false;

                if (shortName != null) {
                    appendLine(builder,
                               "Non-public, non-adminCenter feature [ ", featureName, " ]",
                               " has short name [ ", shortName, " ]");
                }
            }

            if (!isPublic) {
                if (featureInfo.isSetDisableOnConflict() && featureInfo.isAutoFeature()) {
                    appendLine(builder,
                               "Non-public auto feature [ ", featureName, " ]",
                               " has disallowed [ ", FeatureConstants.WLP_DISABLE_ALL_FEATURES_ON_CONFLICT, " ]");
                }

                if (featureInfo.isSetAlsoKnownAs()) {
                    appendLine(builder,
                               "Non-public feature [ ", featureName, " ]",
                               " has non-null [ ", FeatureConstants.WLP_ALSO_KNOWN_AS, " ]",
                               " [ ", featureInfo.getAlsoKnownAs(), " ]");
                }
            }

            File firstParent = featureInfo.getFeatureFile().getParentFile();
            String firstParentName = firstParent.getName();

            String subDirName;
            String categoryName;

            if (inSubDir) {
                subDirName = firstParentName;
                File secondParent = firstParent.getParentFile();
                categoryName = ((secondParent == null) ? null : secondParent.getName());
            } else {
                subDirName = null;
                categoryName = firstParentName;
            }

            if (!categoryName.equals(fileVisibility)) {
                appendLine(builder,
                           "Feature [ ", featureName, " ]",
                           " in [ ", categoryName, " ]",
                           " should be in [ ", fileVisibility, " ]");
            }

            if (inSubDir) {
                if (!subDirName.equals(shortName)) {
                    appendLine(builder,
                               "Feature [ ", featureName, " ] in [ ", subDirName, " ]",
                               " must be in [ ", shortName, " ]");
                }
            }
        });

        String title = "Visibility Errors:";

        maybeFail(builder, title);
    }

    @Test
    public void testProductEditions() {
        StringBuilder builder = new StringBuilder();

        forEach((FeatureInfo featureInfo) -> {
            if (!verifyEdition(builder, featureInfo)) {
                return;
            }

            String edition = featureInfo.getEdition();
            int editionLevel = featureInfo.getEditionLevel();

            for (String depName : featureInfo.getDependentFeatures().keySet()) {
                FeatureInfo dep = getFeature(depName);
                if (dep == null) {
                    return;
                }

                String depEdition = dep.getEdition();
                int depLevel = dep.getEditionLevel();

                // The levels are carefully set to enable
                // the use of a less than test to determine valid
                // product edition relationships.

                if (depLevel < editionLevel) {
                    appendLine(builder,
                               "Feature [ ", featureInfo.getName(), " ] with edition [ ", edition, " ]",
                               " conflicts with [ " + dep.getName(), " ] with edition [ ", depEdition, " ]");
                }
            }
        });

        String title = "Feature edition errors:";
        maybeFail(builder, title);
    }

    /**
     * Features that are marked ga or beta should be in core or base edition in open liberty.
     * Features that are marked noship should be in full edition. This test validates
     * that the edition is marked correctly.
     */
    public boolean verifyEdition(StringBuilder builder, FeatureInfo featureInfo) {
        String feature = featureInfo.getName();

        String edition = featureInfo.getEdition();
        int editionLevel = featureInfo.getEditionLevel();

        // "base", "core", "full"
        if ((editionLevel != 4) && (editionLevel != 5) && (editionLevel != 0)) {
            appendLine(builder,
                       "Feature [ ", feature, " ] has unsupported edition [ ", edition, " ]");
            return false;

        } else {
            String kind = featureInfo.getKind();
            int kindLevel = featureInfo.getKindLevel();

            if (editionLevel == 0) { // "full"
                if (kindLevel != 0) { // "noship"
                    appendLine(builder,
                               "Feature [ ", feature, " ]",
                               " has edition [ ", edition, " ] and kind [ ", kind, " ]",
                               " but must have kind [ ", FeatureConstants.KIND_NOSHIP, " ]");
                    return false;
                }

            } else {
                if (kindLevel == 0) {
                    appendLine(builder,
                               "Feature [ ", feature, " ]",
                               " has kind [ ", kind, " ] and edition [ ", edition, " ]",
                               " but must have edition [ ", FeatureConstants.EDITION_FULL, " ]");
                    return false;
                }
            }
        }

        return true;
    }

    @Test
    public void testProductKinds() {
        StringBuilder builder = new StringBuilder();

        forEach((FeatureInfo featureInfo) -> {
            String feature = featureInfo.getName();

            String kind = featureInfo.getKind();
            int kindLevel = featureInfo.getKindLevel();
            if ((kindLevel != 0) && (kindLevel != 1) && (kindLevel != 2)) {
                appendLine(builder,
                           "Feature [ ", feature, " ] has unsupported kind [ ", kind, " ]");
                return;
            }

            // The levels are carefully set to enable
            // the use of a less than test to determine valid
            // product kind relationships.

            featureInfo.forEachResolvedDep(getRepository(), (FeatureInfo dep) -> {
                int depLevel = dep.getKindLevel();
                if (depLevel < kindLevel) {
                    appendLine(builder,
                               "Feature [ ", feature, " ] with kind [ ", kind, " ]",
                               " conflicts with [ ", dep.getName(), " ] with kind [ ", dep.getKind(), " ]");
                }
            });
        });

        String title = "Feature kind conflicts:";
        maybeFail(builder, title);
    }

    /**
     * Verify that features that start with the same base names are singletons:
     *
     * Auto features must not be singletons.
     *
     * Non-singleton features must not have cohorts -- other features which have
     * the same base name.
     *
     * Versionless features add a new case. For example,
     *
     * "io.openliberty.persistence" which has the same base name as "io.openliberty.persistence-3.0",
     * "io.openliberty.persistence-3.1", and "io.openliberty.persistence-3.2", is not marked as
     * a singleton. This fails the prior cohorts test.
     */
    @Test
    public void testSingleton() {
        StringBuilder builder = new StringBuilder();

        String title = "Feature singleton errors:";

        forEach((FeatureInfo featureInfo) -> {
            if (featureInfo.isAutoFeature() && featureInfo.isSingleton()) {
                appendLine(builder,
                           "Auto-feature [ ", featureInfo.getName(), " ] incorrectly marked as singleton.");
            }
        });

        Map<String, Map<String, List<FeatureInfo>>> visibilityPartitions = getRepository().getVisibilityPartitions();

        visibilityPartitions.forEach((String baseName, Map<String, List<FeatureInfo>> partition) -> {
            partition.forEach((String visibility, List<FeatureInfo> element) -> {
                if (element.size() <= 1) {
                    return;
                }

                element.forEach((FeatureInfo featureInfo) -> {
                    // Normally, a feature which has multiple versions must be a singleton.
                    //
                    // Versionless features are a special case, and are allowed to be non-singletons.
                    //
                    // For example:
                    //
                    // "io.openliberty.persistence"     (versionless, !singleton)
                    // "io.openliberty.persistence-3.0" (versioned, 3.0, singleton)
                    // "io.openliberty.persistence-3.1" (versioned, 3.1, singleton)
                    // "io.openliberty.persistence-3.2" (versioned, 3.2, singleton)

                    if (!featureInfo.isSingleton() && !featureInfo.isVersionless()) {
                        appendLine(builder,
                                   "Non-singleton [ ", featureInfo.getName(), " ] has [ ", Integer.toString(element.size()), " ] cohorts");
                    }
                });
            });
        });

        maybeFail(builder, title);
    }

    @Test
    public void testDependingOnAutoFeature() {
        StringBuilder builder = new StringBuilder();

        String title = "Auto-feature errors:";

        forEach((FeatureInfo featureInfo) -> {
            featureInfo.forEachResolvedDep(getRepository(), (FeatureInfo dep) -> {
                if (dep.isAutoFeature()) {
                    appendLine(builder,
                               "Feature [ ", featureInfo.getName(), " ]",
                               " depends on auto-feature [ ", dep.getName(), " ]");
                }
            });
        });

        maybeFail(builder, title);
    }

    // commented out for now.  Security function doesn't work well with parallel activation
    // currently, but can re-enable at times to see how things are looking and find places
    // where new features were added and parallel activation should match.

    // Parallel activation [ io.openliberty.batchSecurity-2.0 ] conflicts with [ com.ibm.wsspi.appserver.webBundleSecurity-1.0 ]
    // Parallel activation [ com.ibm.websphere.appserver.connectionManagement-1.0 ] conflicts with [ com.ibm.websphere.appserver.transaction-1.1 ]
    // Parallel activation [ com.ibm.websphere.appserver.internal.jca-1.6 ] conflicts with [ com.ibm.websphere.appserver.transaction-1.1 ]
    // Parallel activation [ com.ibm.websphere.appserver.jcaSecurity-1.0 ] conflicts with [ com.ibm.websphere.appserver.transaction-1.1 ]
    // Parallel activation [ io.openliberty.connectionManager1.0.internal.ee-6.0 ] conflicts with [ com.ibm.websphere.appserver.transaction-1.1 ]
    // Parallel activation [ io.openliberty.jakarta.annotation-3.0 ] conflicts with [ io.openliberty.noShip-1.0 ]
    // Parallel activation [ io.openliberty.jakarta.cdi-4.1 ] conflicts with [ io.openliberty.noShip-1.0 ]
    // Parallel activation [ io.openliberty.jakarta.concurrency-3.1 ] conflicts with [ io.openliberty.noShip-1.0 ]
    // Parallel activation [ io.openliberty.jakarta.expressionLanguage-6.0 ] conflicts with [ io.openliberty.noShip-1.0 ]
    // Parallel activation [ io.openliberty.jakarta.faces-5.0 ] conflicts with [ io.openliberty.noShip-1.0 ]
    // Parallel activation [ io.openliberty.jakarta.interceptor-2.2 ] conflicts with [ io.openliberty.noShip-1.0 ]
    // Parallel activation [ io.openliberty.jakarta.nosql-1.0 ] conflicts with [ io.openliberty.noShip-1.0 ]
    // Parallel activation [ io.openliberty.jakarta.pages-4.0 ] conflicts with [ io.openliberty.noShip-1.0 ]
    // Parallel activation [ io.openliberty.jakarta.persistence.base-3.2 ] conflicts with [ io.openliberty.noShip-1.0 ]
    // Parallel activation [ io.openliberty.jakarta.restfulWS-4.0 ] conflicts with [ io.openliberty.noShip-1.0 ]
    // Parallel activation [ io.openliberty.jakarta.validation-3.1 ] conflicts with [ io.openliberty.noShip-1.0 ]
    // Parallel activation [ io.openliberty.jakarta.websocket-2.2 ] conflicts with [ io.openliberty.noShip-1.0 ]
    // Parallel activation [ io.openliberty.jcaSecurity.internal.ee-6.0 ] conflicts with [ com.ibm.websphere.appserver.transaction-1.1 ]
    // Parallel activation [ io.openliberty.jsonbImpl-2.0.0 ] conflicts with [ com.ibm.websphere.appserver.bells-1.0 ]
    // Parallel activation [ io.openliberty.jsonbImpl-3.0.0 ] conflicts with [ com.ibm.websphere.appserver.bells-1.0 ]
    // Parallel activation [ io.openliberty.jsonpImpl-2.0.0 ] conflicts with [ com.ibm.websphere.appserver.bells-1.0 ]
    // Parallel activation [ io.openliberty.jsonpImpl-2.1.0 ] conflicts with [ com.ibm.websphere.appserver.bells-1.0 ]
    // Parallel activation [ io.openliberty.persistentExecutor.internal.ee-7.0 ] conflicts with [ com.ibm.websphere.appserver.persistentExecutorSubset-1.0 ]
    // Parallel activation [ io.openliberty.servlet.api-6.1 ] conflicts with [ io.openliberty.noShip-1.0 ]
    // Parallel activation [ io.openliberty.batch-2.1 ] conflicts with [ io.openliberty.batch2.1.internal.ee-10.0 ]
    // Parallel activation [ com.ibm.websphere.appserver.jaxws-2.2 ] conflicts with [ com.ibm.websphere.appserver.jaxb-2.2 ]
    // Parallel activation [ com.ibm.websphere.appserver.jaxws-2.2 ] conflicts with [ com.ibm.websphere.appserver.javax.mail-1.5 ]
    // Parallel activation [ com.ibm.websphere.appserver.managedBeans-1.0 ] conflicts with [ com.ibm.websphere.appserver.transaction-1.1 ]
    // Parallel activation [ io.openliberty.messagingSecurity-3.0 ] conflicts with [ com.ibm.websphere.appserver.security-1.0 ]
    // Parallel activation [ io.openliberty.mpGraphQL-2.0 ] conflicts with [ io.openliberty.mpContextPropagation-1.3 ]
    // Parallel activation [ com.ibm.websphere.appserver.mpJwt-1.0 ] conflicts with [ com.ibm.websphere.appserver.jwt-1.0 ]
    // Parallel activation [ com.ibm.websphere.appserver.mpJwt-1.0 ] conflicts with [ com.ibm.websphere.appserver.org.eclipse.microprofile.jwt-1.0 ]
    // Parallel activation [ com.ibm.websphere.appserver.mpJwt-1.0 ] conflicts with [ com.ibm.websphere.appserver.appSecurity-2.0 ]
    // Parallel activation [ com.ibm.websphere.appserver.mpJwt-1.1 ] conflicts with [ com.ibm.websphere.appserver.jwt-1.0 ]
    // Parallel activation [ com.ibm.websphere.appserver.mpJwt-1.1 ] conflicts with [ com.ibm.websphere.appserver.org.eclipse.microprofile.jwt-1.0 ]
    // Parallel activation [ com.ibm.websphere.appserver.mpJwt-1.1 ] conflicts with [ com.ibm.websphere.appserver.appSecurity-2.0 ]
    // Parallel activation [ com.ibm.websphere.appserver.mpJwt-1.2 ] conflicts with [ com.ibm.websphere.appserver.jwt-1.0 ]
    // Parallel activation [ com.ibm.websphere.appserver.mpJwt-1.2 ] conflicts with [ com.ibm.websphere.appserver.appSecurity-3.0 ]
    // Parallel activation [ io.openliberty.mpJwt-2.0 ] conflicts with [ com.ibm.websphere.appserver.jwt-1.0 ]
    // Parallel activation [ io.openliberty.mpJwt-2.0 ] conflicts with [ io.openliberty.appSecurity-4.0 ]
    // Parallel activation [ io.openliberty.mpJwt-2.1 ] conflicts with [ com.ibm.websphere.appserver.jwt-1.0 ]

    // @Test
    public void testParallelActivation() {
        StringBuilder builder = new StringBuilder();

        forEach((FeatureInfo featureInfo) -> {
            if (!featureInfo.isParallelActivationEnabled()) {
                return;
            }

            featureInfo.forEachResolvedDep(getRepository(), (FeatureInfo dep) -> {
                if (!dep.isParallelActivationEnabled()) {
                    appendLine(builder,
                               "Parallel activation [ ", featureInfo.getName() + " ]",
                               " conflicts with [ ", dep.getName(), " ]");
                }
            });
        });

        String title = "Parallel activation errors:";
        maybeFail(builder, title);
    }

    /**
     * Locate features which have disable-on-conflict-enabled disabled,
     * and which have dependencies which have disable-on-conflict-enabled enabled.
     *
     * @param conflicts Storage for conflicts.
     */
    protected void validateOnConflicts(Map<FeatureInfo, Set<FeatureInfo>> conflicts) {
        StringBuilder builder = new StringBuilder();

        forEach((FeatureInfo featureInfo) -> {
            if (featureInfo.isDisableOnConflictEnabled()) {
                return;
            }

            featureInfo.forEachResolvedDep(getRepository(), (FeatureInfo dep) -> {
                if (dep.isDisableOnConflictEnabled()) {
                    appendLine(builder,
                               "  Feature [ ", featureInfo.getName() + " ] is not enabled;",
                               "  dependent [ ", dep.getName(), " ] is enabled");
                }
            });
        });

        String title = "Feature disable-on-conflict errors:";
        maybeFail(builder, title);
    }

    // Servlet 3.0 and Rest Connector 1.0 are present only
    // in Commercial Liberty.  Many existing features depend on
    // Servlet 3.0.  A single feature, io.openliberty.adminCenter1.0.javaee,
    // depends on Rest Connector 1.0.
    //
    // That these features is not present is possible because of tolerates
    // clauses which are enabled for later feature versions which are present.
    //
    // For example,
    // "io.openliberty.admincenter1.0.javaee" has:
    //
    // -features=
    //   com.ibm.websphere.appserver.restConnector-1.0; ibm.tolerates:="2.0",
    //   com.ibm.websphere.appserver.jta-1.1; ibm.tolerates:="1.2",
    //   com.ibm.websphere.appserver.jsp-2.2; ibm.tolerates:="2.3",
    //   com.ibm.websphere.appserver.servlet-3.0; ibm.tolerates:="3.1,4.0"
    //
    // Rest Connector 2.0 and Servlet 3.1 are both present in Open Liberty.

    public static final String FEATURE_SERVLET_30 = "com.ibm.websphere.appserver.servlet-3.0";
    public static final String FEATURE_SERVLET_INTERNAL_30 = "io.openliberty.servlet.internal-3.0";

    public static final String FEATURE_RESTCONNECTOR_10 = "com.ibm.websphere.appserver.restConnector-1.0";

    // Used by com.ibm.websphere.appserver.jcaInboundSecurity-1.0
    public static final String FEATURE_JCA_16 = "com.ibm.websphere.appserver.jca-1.6";

    public static boolean permitAbsence(String featureName) {
        return (featureName.equals(FEATURE_SERVLET_30) ||
                featureName.equals(FEATURE_SERVLET_INTERNAL_30) ||
                featureName.equals(FEATURE_RESTCONNECTOR_10) ||
                featureName.equals(FEATURE_JCA_16));
    }

    @Test
    public void testDependencies() {
        StringBuilder builder = new StringBuilder();

        List<String> missingDeps = new ArrayList<>();

        forEach((FeatureInfo featureInfo) -> {
            missingDeps.clear();

            featureInfo.forEachDepName((String depName) -> {
                if (getFeature(depName) == null) {
                    if (!permitAbsence(depName)) {
                        missingDeps.add(depName);
                    }
                }
            });

            if (!missingDeps.isEmpty()) {
                appendLine(builder,
                           "Feature [ ", featureInfo.getName(), " ]",
                           " unresolved [ ", missingDeps.toString(), " ]");
            }
        });

        String title = "Missing dependents errors:";
        maybeFail(builder, title);
    }

    /**
     * Tests that an auto feature has more than one feature in its filter.
     */
    @Test
    public void testAutoFeatures() {
        StringBuilder builder = new StringBuilder();

        forEach((FeatureInfo featureInfo) -> {
            if (!featureInfo.isAutoFeature()) {
                return;
            }

            Set<String> autoFeatures = featureInfo.getAutoFeatures();
            if (autoFeatures.size() > 1) {
                return;
            }

            String featureName = featureInfo.getName();

            if (autoFeatures.isEmpty()) {
                appendLine(builder,
                           "  Auto feature [ ", featureName, " ] has no cohorts.");

            } else {
                String auto = autoFeatures.iterator().next();

                appendLine(builder,
                           "  Auto feature [ ", featureName, " ]",
                           " has exactly one cohort [ ", auto, " ]");
                appendLine(builder,
                           "The cohort should be made a dependency feature",
                           " or a dependency private feature.");
            }
        });

        String title = "Auto-feature errors:";
        maybeFail(builder, title);
    }

    /**
     * This test makes sure that public features have properties files that match the long
     * feature name. When moving features to the io.openliberty prefix from com.ibm.websphere.appserver
     * and vice versa, the properties file renames were missed a few times. This unit test
     * makes sure that it is found in the build instead of having to be detected by hand.
     */
    @Test
    public void testLocalizationResources() {
        StringBuilder builder = new StringBuilder();

        forEach((FeatureInfo featureInfo) -> {
            String featureName = featureInfo.getName();

            if (!featureInfo.isPublic()) {
                // AdminCenter features have resources.
                if (!featureName.startsWith("com.ibm.websphere.appserver.adminCenter.tool")) {
                    return;
                }
            }

            String propertiesPath = "resources/l10n/" + featureName + ".properties";

            File featureFile = featureInfo.getFeatureFile();
            File resourceFile = new File(featureFile.getParentFile(), propertiesPath);

            if (!resourceFile.exists()) {
                appendLine(builder,
                           "Feature [ ", featureName, " ] missing expected resources [ ", resourceFile.getPath(), " ]");
            }
        });

        String title = "Feature localization error:";
        maybeFail(builder, title);
    }

    //

    protected static final Set<String> disallowedNoShipFeatures;

    // Features no-teExpected no-ship features.

    static {
        disallowedNoShipFeatures = new HashSet<>(1);
        disallowedNoShipFeatures.add("io.openliberty.persistentExecutor.internal.ee-10.0");
    }

    @Test
    public void testNoShipFeatures() {
        StringBuilder builder = new StringBuilder();

        disallowedNoShipFeatures.forEach((String feature) -> {
            FeatureInfo featureInfo = getFeature(feature);
            if (featureInfo == null) {
                appendLine(builder,
                           "  Missing disallowed no-ship [ ", feature, " ]");

            } else {
                if (!featureInfo.isNoShip()) {
                    appendLine(builder,
                               "  Expected no-ship [ ", feature, " ]",
                               " is now [ ", featureInfo.getKind(), " ]");
                    appendLine(builder,
                               "  Remove this feature from the disallowed no-ship features list.");
                }
            }
        });

        forEach((FeatureInfo featureInfo) -> {
            if (featureInfo.isNoShip() && !featureInfo.isAutoFeature()) {
                boolean containsNoShip = false;
                boolean containsBeta = false;

                for (String dep : featureInfo.getDependentFeatures().keySet()) {
                    FeatureInfo depFeature = getFeature(dep);
                    if (depFeature == null) {
                        continue;
                    }

                    if (!containsNoShip) {
                        containsNoShip = depFeature.isNoShip();
                    }
                    if (!containsBeta) {
                        containsBeta = depFeature.isBeta();
                    }
                }

                // Found features that are marked noship, but contain only beta/ga features without a noship feature dependency:
                // If you recently marked a feature beta, you may need to update the feature to depend on noShip-1.0 feature,
                // add or remove from the expected failures list in this test, or have something to fix.

                if (!containsNoShip && containsBeta) {
                    String featureName = featureInfo.getBaseName();
                    if (!disallowedNoShipFeatures.contains(featureName)) {
                        appendLine(builder,
                                   "  No-ship auto feature [ ", featureName, " ]",
                                   " has no no-ship dependencies and has beta dependencies");
                    }
                }
            }
        });

        String title = "Beta/No-Ship feature errors:";
        maybeFail(builder, title);
    }

    /**
     * Ensure that public and protected features are correctly referenced in a feature
     * when a dependent feature includes a public or protected feature with a tolerates attribute.
     */
    @Test
    public void testNonTransitiveTolerates() {
        StringBuilder builder = new StringBuilder();

        Map<String, String> baseVisibilities = getBaseVisibilities();

        Set<String> processed = new HashSet<>();

        forEach((FeatureInfo rootInfo) -> {
            String root = rootInfo.getName();

            processed.clear();

            Map<String, Set<String>> errors = new HashMap<>();

            Map<String, Attrs> deps = rootInfo.getDependentFeatures();

            Set<String> depsWithoutTolerates = new HashSet<>();
            deps.forEach((String dep, Attrs attrs) -> {
                if (!attrs.containsKey(FeatureConstants.IBM_TOLERATES)) {
                    depsWithoutTolerates.add(dep);
                }
            });

            Map<String, Set<String>> toleratedFeatures = new HashMap<>();

            deps.forEach((String dep, Attrs depAttrs) -> {
                FeatureInfo depInfo = getFeature(dep);
                if (depInfo == null) {
                    return;
                }

                boolean depTolerates = depAttrs.containsKey(FeatureConstants.IBM_TOLERATES);

                depInfo.getDependentFeatures().forEach((String depOfDep, Attrs depOfDepAttrs) -> {
                    FeatureInfo depOfDepInfo = getFeature(depOfDep);
                    if (depOfDepInfo == null) {
                        return;
                    }

                    boolean depOfDepTolerates = depOfDepAttrs.containsKey(FeatureConstants.IBM_TOLERATES);

                    if (!depOfDepTolerates && processed.contains(depOfDep)) {
                        return;
                    }

                    String parentPath = root + " -> " + dep;

                    boolean isApiJarFalse = false;

                    Map<String, Set<String>> tolFeatures = processDependent(processed,
                                                                            depsWithoutTolerates,
                                                                            parentPath, depTolerates,
                                                                            depOfDep, depOfDepTolerates,
                                                                            isApiJarFalse,
                                                                            errors);
                    if (tolFeatures == null) {
                        return;
                    }

                    for (Entry<String, Set<String>> entry2 : tolFeatures.entrySet()) {
                        String key = entry2.getKey();
                        Set<String> existing = toleratedFeatures.get(key);
                        if (existing == null) {
                            toleratedFeatures.put(key, entry2.getValue());
                        } else {
                            existing.addAll(entry2.getValue());
                        }
                    }
                });
            });

            if (toleratedFeatures.isEmpty()) {
                return;
            }

            for (String dep : deps.keySet()) {
                toleratedFeatures.remove(FeatureInfo.getBaseName(dep));
            }

            if (toleratedFeatures.isEmpty()) {
                return;
            }

            // appSecurity is special because they is dependencies on each other.
            String nonSingletonToleratedFeature = "com.ibm.websphere.appserver.appSecurity";

            for (Iterator<String> features = toleratedFeatures.keySet().iterator(); features.hasNext();) {
                String featureBase = features.next();
                if (featureBase.contentEquals(nonSingletonToleratedFeature) ||
                    "private".equals(baseVisibilities.get(featureBase))) {
                    features.remove();
                }
            }

            toleratedFeatures.forEach((String baseName, Set<String> features) -> {
                appendLine(builder,
                           "Feature [ ", root, " ]",
                           "must have a tolerated dependency [ ", baseName, " ] ",
                           "within [ ", features.toString(), " ]");
            });
        });

        String title = "Non-transive feature tolerates errors:";
        maybeFail(builder, title);
    }

    /**
     * Finds private and protected dependent features that
     * are redundant because other dependent features already bring them in.
     *
     * Public features are not included in this test since those may be
     * explicitly included just to show which public features are enabled by a feature.
     */
    @Test
    public void testFeatureDependenciesRedundancy() {
        StringBuilder builder = new StringBuilder();

        Map<String, String> baseVisibilities = getBaseVisibilities();

        forEach((FeatureInfo featureInfo) -> {
            String featureName = featureInfo.getName();

            Set<String> processedFeatures = new HashSet<>();

            Map<String, Attrs> depFeatures = featureInfo.getDependentFeatures();

            Set<String> rootDepFeatureWithoutTolerates = new HashSet<>();
            depFeatures.forEach((String dep, Attrs depAttrs) -> {
                if (!depAttrs.containsKey(FeatureConstants.IBM_TOLERATES)) {
                    rootDepFeatureWithoutTolerates.add(dep);
                }
            });

            Map<String, Set<String>> errors = new HashMap<>();

            Set<String> toleratedFeatures = new HashSet<>();

            depFeatures.forEach((String depFeatureName, Attrs depAttrs) -> {
                FeatureInfo depFeatureInfo = getFeature(depFeatureName);
                if (depFeatureInfo == null) {
                    return;
                }

                boolean depApiJarFalse = "false".equals(depAttrs.get("apiJar"));
                boolean depTolerates = depAttrs.containsKey(FeatureConstants.IBM_TOLERATES);

                depFeatures.forEach((String depOfDep, Attrs depOfDepAttrs) -> {
                    boolean depOfDepTolerates = depOfDepAttrs.containsKey(FeatureConstants.IBM_TOLERATES);

                    boolean depOfDepApiJarFalse = "false".equals(depOfDepAttrs.get("apiJar"));

                    String parentPath = featureName + " -> " + depFeatureName;

                    boolean apiJarFalse = depApiJarFalse || depOfDepApiJarFalse;

                    Map<String, Set<String>> tolFeatures = processDependencies(featureName,
                                                                               processedFeatures,
                                                                               rootDepFeatureWithoutTolerates,
                                                                               parentPath,
                                                                               depTolerates,
                                                                               depOfDep, depOfDepTolerates,
                                                                               apiJarFalse,
                                                                               errors);
                    if (tolFeatures != null) {
                        toleratedFeatures.addAll(tolFeatures.keySet());
                    }
                });
            });

            errors.forEach((String depFeature, Set<String> errorPaths) -> {
                String baseFeatureName = FeatureInfo.getBaseName(depFeature);

                if (toleratedFeatures.contains(baseFeatureName)) {
                    return;
                }

                // Problem: netty, which consists of
                // io.openliberty.io.netty, io.openliberty.io.netty.ssl,
                // and io.openliberty.netty.internal-1.0,
                // has two versionless features.
                //
                // Work-around this for now with a check for a null base
                // visibility.

                String baseVisibility = baseVisibilities.get(baseFeatureName);
                if ((baseVisibility != null) && baseVisibility.equals(FeatureConstants.VISIBILITY_PUBLIC)) {
                    return;
                }

                appendLine(builder, "Feature [ ", depFeature, " ] has redundant features:");
                for (String errorPath : errorPaths) {
                    appendLine(builder, "  [ ", errorPath, " ]");
                }
            });
        });

        String title = "Feature redundency errors:";
        maybeFail(builder, title);
    }

    private Map<String, Set<String>> processDependencies(String root,
                                                         Set<String> processed, Set<String> depsWithoutTolerates,
                                                         String parentPath, boolean hasToleratesAncestor,
                                                         String depOfDep, boolean depOfDepTolerates,
                                                         boolean isApiJarFalse,
                                                         Map<String, Set<String>> errors) {

        Map<String, Set<String>> toleratedFeatures = processDependent(processed,
                                                                      depsWithoutTolerates,
                                                                      parentPath, hasToleratesAncestor,
                                                                      depOfDep, depOfDepTolerates,
                                                                      isApiJarFalse,
                                                                      errors);

        FeatureInfo depOfDepInfo = getFeature(depOfDep);
        if (depOfDepInfo == null) {
            return toleratedFeatures;
        }

        // Correct this for the next level of dependents.
        hasToleratesAncestor |= depOfDepTolerates;

        for (Map.Entry<String, Attrs> depEntry : depOfDepInfo.getDependentFeatures().entrySet()) {
            String nextDep = depEntry.getKey();
            Attrs nextAttrs = depEntry.getValue();

            String nextParentPath = parentPath + " -> " + depOfDep;

            boolean nextTolerates = nextAttrs.containsKey(FeatureConstants.IBM_TOLERATES);

            boolean depApiJarFalse = "false".equals(nextAttrs.get("apiJar"));
            boolean nextApiJarFalse = isApiJarFalse || depApiJarFalse;

            Map<String, Set<String>> includeTolerates = processDependencies(root,
                                                                            processed, depsWithoutTolerates,
                                                                            nextParentPath, hasToleratesAncestor,
                                                                            nextDep, nextTolerates,
                                                                            nextApiJarFalse,
                                                                            errors);

            if (includeTolerates == null) {
                continue;
            }

            if (toleratedFeatures == null) {
                toleratedFeatures = new HashMap<>(includeTolerates);
            } else {
                for (Entry<String, Set<String>> entry : includeTolerates.entrySet()) {
                    String key = entry.getKey();
                    Set<String> existing = toleratedFeatures.get(key);
                    if (existing == null) {
                        toleratedFeatures.put(key, entry.getValue());
                    } else {
                        existing.addAll(entry.getValue());
                    }
                }
            }
        }

        return toleratedFeatures;
    }

    private Map<String, Set<String>> processDependent(Set<String> processed,
                                                      Set<String> depsWithoutTolerates,
                                                      String parentPath, boolean hasToleratesAncestor,
                                                      String depOfDep, boolean depOfDepTolerates,
                                                      boolean isApiJarFalse,
                                                      Map<String, Set<String>> errors) {

        processed.add(depOfDep);

        if (depOfDepTolerates) {
            HashSet<String> depWithTolerate = new HashSet<>();
            depWithTolerate.add(parentPath);

            Map<String, Set<String>> tolerated = new HashMap<>();
            tolerated.put(FeatureInfo.getBaseName(depOfDep), depWithTolerate);

            return tolerated;
        }

        if (!hasToleratesAncestor &&
            !isApiJarFalse &&
            depsWithoutTolerates.contains(depOfDep) &&
            !(depOfDep.startsWith("com.ibm.websphere.appserver.eeCompatible-") ||
              depOfDep.startsWith("io.openliberty.mpCompatible-") ||
              depOfDep.startsWith("io.openliberty.servlet.internal-"))) {

            Set<String> problemPaths = errors.computeIfAbsent(depOfDep,
                                                              (String useFeature) -> new HashSet<>());
            problemPaths.add(parentPath);
        }

        return null;
    }

    //

    protected static void maybeFail(StringBuilder builder, String title) {
        if (builder.length() != 0) {
            builder.insert(0, title);
            // The character of the builder is required to be a new line.

            Assert.fail(builder.toString());
        }
    }

    protected static StringBuilder ensureTitle(StringBuilder builder, String title, String... values) {
        ensureTitle(builder, title);
        appendLine(builder, values);
        return builder;
    }

    protected static StringBuilder ensureTitle(StringBuilder builder, String title) {
        if (builder.length() == 0) {
            builder.append(title);
        }
        return builder;
    }

    protected static StringBuilder append(StringBuilder builder, String... values) {
        for (String value : values) {
            builder.append(value);
        }
        return builder;
    }

    protected static StringBuilder appendLine(StringBuilder builder, String... values) {
        builder.append('\n');

        for (String value : values) {
            builder.append(value);
        }
        return builder;
    }
}
