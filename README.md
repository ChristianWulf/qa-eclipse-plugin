# Lightweight Eclipse Plugin for Quality Assurance Tools
The offical Eclipse plugins for PMD and Checkstyle work great...as long as you do not want to add custom rules or checks, respectively. For this purpose, you need to implement an Eclipse Plugin Fragment including the new rules/checks. This causes an unnecessarily high implementation effort. Hence, I implemented a new plugin, which covers all matured qa tools, and allows an easy integration of custom rules/checks via a config file. Eclipse Plugin Fragments are not necessary. You can keep your qa config files and use them for both your continuous integration infrastructure and within Eclipse.

- Current support: PMD
- In progress: Checkstyle, Findbugs

## Usage
The plugin provides for each qa tool:
- a command entry in the context menu (a.k.a. right-click menu) to execute the tool (example: "Run PMD")
- a view to display the issues detected by the tool. This view needs to be opened manually via "Settings -> Open View -> ...".

## Install
- via Eclipse Updatesite: https://build.se.informatik.uni-kiel.de/eus/qa/

## Build
- via Maven 3.3 or above (necessary for pom-less building):
```
mvn clean package
```
