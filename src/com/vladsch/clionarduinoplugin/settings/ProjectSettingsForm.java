/*
 * Copyright (c) 2015-2016 Vladimir Schneider <vladimir.schneider@gmail.com>, all rights reserved.
 *
 * This code is private property of the copyright holder and cannot be used without
 * having obtained a license or prior written permission of the of the copyright holder.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package com.vladsch.clionarduinoplugin.settings;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.vladsch.clionarduinoplugin.components.ArduinoProjectSettings;
import com.vladsch.clionarduinoplugin.util.ui.Settable;
import com.vladsch.clionarduinoplugin.util.ui.SettingsComponents;
import org.jetbrains.annotations.NotNull;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProjectSettingsForm implements Disposable, RegExSettingsHolder {
    private static final Logger logger = Logger.getInstance("com.vladsch.clionarduinoplugin.settings");

    JPanel myMainPanel;
    JComboBox myPort;
    JComboBox myBaudRate;
    JBCheckBox myDisconnectOnBuild;
    JBCheckBox myReconnectAfterBuild;
    JBTextField myBuildConfigurationNames;
    JComboBox myBuildConfigurationPattern;
    private JButton myEditRegExButton;
    JBCheckBox myAfterSuccessfulBuild;
    JBCheckBox myLogConnectDisconnect;
    JBCheckBox myActivateOnConnect;

    private @NotNull String myRegexSampleText;
    SerialPortNames.EnumLike mySerialPortNames;

    public JComponent getComponent() {
        return myMainPanel;
    }

    private final SettingsComponents<ArduinoProjectSettings> components;

    public ProjectSettingsForm(ArduinoProjectSettings settings) {
        components = new SettingsComponents<ArduinoProjectSettings>() {
            @Override
            protected Settable[] getComponents(ArduinoProjectSettings i) {
                return new Settable[] {
                        componentString(mySerialPortNames.ADAPTER, myPort, i::getPort, i::setPort),
                        component(SerialBaudRates.ADAPTER, myBaudRate, i::getBaudRate, i::setBaudRate),
                        component(myDisconnectOnBuild, i::isDisconnectOnBuild, i::setDisconnectOnBuild),
                        component(myLogConnectDisconnect, i::isLogConnectDisconnect, i::setLogConnectDisconnect),
                        component(myActivateOnConnect, i::isActivateOnConnect, i::setActivateOnConnect),
                        component(myAfterSuccessfulBuild, i::isAfterSuccessfulBuild, i::setAfterSuccessfulBuild),
                        component(BuildConfigurationPatternType.ADAPTER, myBuildConfigurationPattern, i::getBuildConfigurationPatternType, i::setBuildConfigurationPatternType),
                        component(myBuildConfigurationNames, i::getBuildConfigurationNames, i::setBuildConfigurationNames),
                };
            }
        };

        myRegexSampleText = settings.getRegexSampleText();

        final ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {updateOptions(false);}
        };

        myReconnectAfterBuild.addActionListener(actionListener);

        myEditRegExButton.addActionListener(e -> {
            boolean valid = RegExTestDialog.showDialog(myMainPanel, this);
            //myRemovePrefixOnPaste.setSelected(valid);
            //myAddPrefixOnPaste.setSelected(valid);
        });
    }

    void updateOptions(boolean onInit) {
        BuildConfigurationPatternType selectedType = BuildConfigurationPatternType.ADAPTER.get(myBuildConfigurationPattern);
        final boolean regexPrefixes = selectedType == BuildConfigurationPatternType.REGEX;
        final boolean all = selectedType == BuildConfigurationPatternType.ALL;
        boolean enablePrefixes = !all && !regexPrefixes;

        myBuildConfigurationNames.setEnabled(enablePrefixes);
        myEditRegExButton.setVisible(regexPrefixes);

        myAfterSuccessfulBuild.setEnabled(myReconnectAfterBuild.isSelected());
    }

    // @formatter:off
    @NotNull @Override public String getPatternText() { return myBuildConfigurationNames.getText().trim(); }
    @Override public void setPatternText(final String patternText) { myBuildConfigurationNames.setText(patternText); }

    @NotNull @Override public String getSampleText() { return myRegexSampleText; }
    @Override public void setSampleText(final String sampleText) {
        myRegexSampleText = sampleText;
    }
    @Override public boolean isCaseSensitive() { return true; }
    @Override public boolean isBackwards() { return false; }
    @Override public void setCaseSensitive(final boolean isCaseSensitive) { }
    @Override public void setBackwards(final boolean isBackwards) { }
    @Override public boolean isCaretToGroupEnd() { return false; }
    @Override public void setCaretToGroupEnd(final boolean isCaretToGroupEnd) { }
    // @formatter:on

    private void createUIComponents() {
        final ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {updateOptions(false);}
        };

        mySerialPortNames = new SerialPortNames.EnumLike(false);

        myPort = mySerialPortNames.ADAPTER.createComboBox();
        myBaudRate = SerialBaudRates.ADAPTER.createComboBox();

        myBuildConfigurationPattern = BuildConfigurationPatternType.ADAPTER.createComboBox();
        myBuildConfigurationPattern.addActionListener(actionListener);
    }

    public boolean isModified(@NotNull ArduinoProjectSettings settings) {
        return components.isModified(settings) || !myRegexSampleText.equals(settings.getRegexSampleText());
    }

    public void apply(@NotNull ArduinoProjectSettings settings) {
        components.apply(settings);
        settings.setRegexSampleText(myRegexSampleText);
        settings.fireSettingsChanged();
    }

    public void reset(@NotNull ArduinoProjectSettings settings) {
        components.reset(settings);
        myRegexSampleText = settings.getRegexSampleText();
        settings.fireSettingsChanged();
    }

    @Override
    public void dispose() {

    }
}