[![GitHub issues](https://img.shields.io/github/issues/ChristianWulf/qa-eclipse-plugin.svg)](https://github.com/ChristianWulf/qa-eclipse-plugin/issues)
[![GitHub license](https://img.shields.io/github/license/ChristianWulf/qa-eclipse-plugin.svg)](https://github.com/ChristianWulf/qa-eclipse-plugin/blob/master/LICENSE)

# Lightweight Eclipse Plugin for Quality Assurance Tools
The offical Eclipse plugins for PMD and Checkstyle work great...as long as you do not want to add custom rules or checks, respectively. For this purpose, you need to implement an Eclipse Plugin Fragment including the new rules/checks. This causes an unnecessarily high implementation effort. Hence, I implemented a new plugin, which covers all matured qa tools, and allows an easy integration of custom rules/checks via a config file. Eclipse Plugin Fragments are not necessary. You can keep your qa config files and use them for your build tool, your continuous integration infrastructure, and within Eclipse.

- Done: PMD, Checkstyle
- In progress: Findbugs
- Planned: nothing else, so far.

 See our [associated milestones](https://github.com/ChristianWulf/qa-eclipse-plugin/milestones).

## Usage
The plugin provides for each qa tool:
- a command entry in the context menu (a.k.a. right-click menu) to execute the tool (example: "Run PMD")
  
  ![Image of the PMD's context menu entry](bundles/pmd/screenshots/pmd_context_menu.png)
- a view to display the issues detected by the tool. This view needs to be opened manually via 
  > ``"Window -> Show View -> Other..."``
  
  ![Image of the PMD's violations view](bundles/pmd/screenshots/pmd_violations_view.png)
- a property page for each project (example: "PMD Settings"). This page allows to configure the tool.
  
  ![Image of the PMD's property page](bundles/pmd/screenshots/pmd_property_page.PNG)

## Configuration
The plugin expects a dedicated "prefs" file in the directory ".settings" of the Eclipse project, you like to check for issues. This file is created automatically if you press the "apply" button on the corresponding property page.

The property page allows
- to enable/disable the tool for the project, 
- to define the path to the tool's xml configuration file, and
- to define the path(s) to the jar file(s) containing your custom rules/checks. 

The "prefs" file is intended to be uploaded to your repository to share your configuration with your team.

**IMPORTANT** The version of the API used for your custom rules/checks must be compatible with the version of the tool included in this Eclipse plugin.
- The version of PMD included in this plugin can be found [here](bundles/pmd/lib).
- The version of Checkstyle included in this plugin can be found [here](bundles/checkstyle/lib)

## Install
- via Eclipse Updatesite:
  - Snapshot version: https://build.se.informatik.uni-kiel.de/eus/qa/snapshot/
  - Release version: not yet available

## Build
- via Maven 3.3 or above (necessary for pom-less building):
```
mvn clean package
```
