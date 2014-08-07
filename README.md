Salesforce Apex Checkstyle Eclipse plugin
==========================

Checkstyle Eclipse plugin based on regexp. The plugin is fully configurable.

You can simplify a checking of your code style by clicking the context menu or saving edited file. If text of your code matches the configured regexp, Eclipse will show a specified warning message.

Installation:
Export JAR file from source code or take it from the repository. Put it into <eclipse_directory>/plugins. Restart or start Eclipse. Put the configuration file named apexcheckstyle-config.xml into <your_workspace>/.metadata/.plugins/com.salesforce.apex.checkstyle.plugin/. Restart eclipse or reload workspace.

It also can be used for another types of files (not only for Apex classes), you have to specify appropriate file extension.

