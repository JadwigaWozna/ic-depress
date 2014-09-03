/*
 ImpressiveCode Depress Framework
 Copyright (C) 2013  ImpressiveCode contributors

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.impressivecode.depress.its.bugzilla;

import static org.impressivecode.depress.its.bugzilla.BugzillaAdapterTableFactory.createTableSpec;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.impressivecode.depress.common.SettingsModelMultiFilter;
import org.impressivecode.depress.its.ITSAdapterTableFactory;
import org.impressivecode.depress.its.ITSAdapterTransformer;
import org.impressivecode.depress.its.ITSDataType;
import org.impressivecode.depress.its.ITSPriority;
import org.impressivecode.depress.its.ITSResolution;
import org.impressivecode.depress.its.ITSStatus;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.google.common.base.Preconditions;

/**
 * @author Marek Majchrzak, ImpressiveCode
 * @author Maciej Borkowski, Capgemini Poland
 */
public class BugzillaAdapterNodeModel extends NodeModel {
    private static final NodeLogger LOGGER = NodeLogger.getLogger(BugzillaAdapterNodeModel.class);

    private static final String CHOOSER_DEFAULT_VALUE = "";

    static final String CHOOSER_CONFIG_NAME = "file chooser";
    static final String PRIORITY_CONFIG_NAME = "priority";
    static final String RESOLUTION_CONFIG_NAME = "resolution";
    static final String STATUS_CONFIG_NAME = "status";

    private final SettingsModelString fileSettings = createFileChooserSettings();

    private final SettingsModelMultiFilter priorityModel = createMultiFilterPriorityModel();
    private final SettingsModelMultiFilter resolutionModel = createMultiFilterResolutionModel();
    private final SettingsModelMultiFilter statusModel = createMultiFilterStatusModel();

    protected BugzillaAdapterNodeModel() {
        super(0, 1);
    }

    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
            throws Exception {
        LOGGER.info("Preparing to read bugzilla entries.");
        String filePath = fileSettings.getStringValue();
        List<ITSDataType> entries = parseEntries(filePath);
        LOGGER.info("Transforming to buzilla entries.");
        BufferedDataTable out = transform(entries, exec);
        LOGGER.info("Bugzilla table created.");
        return new BufferedDataTable[] { out };
    }

    private BufferedDataTable transform(final List<ITSDataType> entries, final ExecutionContext exec)
            throws CanceledExecutionException {
        ITSAdapterTransformer transformer = new ITSAdapterTransformer(ITSAdapterTableFactory.createDataColumnSpec());
        return transformer.transform(entries, exec);
    }

    private List<ITSDataType> parseEntries(final String filePath) throws Exception {
        try {
            return new BugzillaEntriesParser(priorityModel.getIncluded(), resolutionModel.getIncluded(),
                    statusModel.getIncluded()).parseEntries(filePath);
        } catch (Exception e) {
            LOGGER.error("Error during parsing data", e);
            throw e;
        }
    }

    @Override
    protected void reset() {
    }

    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        Preconditions.checkArgument(inSpecs.length == 0);
        return createTableSpec();
    }

    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        fileSettings.saveSettingsTo(settings);
        priorityModel.saveSettingsTo(settings);
        resolutionModel.saveSettingsTo(settings);
        statusModel.saveSettingsTo(settings);
    }

    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        fileSettings.loadSettingsFrom(settings);
        priorityModel.loadSettingsFrom(settings);
        resolutionModel.loadSettingsFrom(settings);
        statusModel.loadSettingsFrom(settings);
    }

    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        fileSettings.validateSettings(settings);
        priorityModel.validateSettings(settings);
        resolutionModel.validateSettings(settings);
        statusModel.validateSettings(settings);
    }

    @Override
    protected void loadInternals(final File internDir, final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // NOOP
    }

    @Override
    protected void saveInternals(final File internDir, final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // NOOP
    }

    static SettingsModelString createFileChooserSettings() {
        return new SettingsModelString(CHOOSER_CONFIG_NAME, CHOOSER_DEFAULT_VALUE);
    }

    static SettingsModelMultiFilter createMultiFilterPriorityModel() {
        return new SettingsModelMultiFilter(PRIORITY_CONFIG_NAME, false, ITSPriority.labels());
    }

    static SettingsModelMultiFilter createMultiFilterResolutionModel() {
        return new SettingsModelMultiFilter(RESOLUTION_CONFIG_NAME, false, ITSResolution.labels());
    }

    static SettingsModelMultiFilter createMultiFilterStatusModel() {
        return new SettingsModelMultiFilter(STATUS_CONFIG_NAME, false, ITSStatus.labels());
    }
}
