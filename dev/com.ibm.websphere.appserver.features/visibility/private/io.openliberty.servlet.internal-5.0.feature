-include= ~${workspace}/cnf/resources/bnd/feature.props
symbolicName=io.openliberty.servlet.internal-5.0
visibility=private
singleton=true
IBM-App-ForceRestart: install, uninstall
Subsystem-Category: JakartaEE9Application
-features=io.openliberty.jakartaeePlatform-9.0, \
  com.ibm.websphere.appserver.eeCompatible-9.0, \
  io.openliberty.servlet.api-5.0, \
  com.ibm.websphere.appserver.javaeeddSchema-1.0, \
  com.ibm.websphere.appserver.injection-2.0, \
  com.ibm.websphere.appserver.httptransport-1.0, \
  com.ibm.websphere.appserver.requestProbes-1.0
-bundles=com.ibm.ws.app.manager.war.jakarta, \
 com.ibm.ws.managedobject, \
 com.ibm.ws.org.apache.commons.io, \
 com.ibm.websphere.security, \
 com.ibm.ws.org.apache.commons.fileupload.jakarta, \
 com.ibm.ws.webcontainer.servlet.4.0.jakarta, \
 com.ibm.ws.webcontainer.servlet.4.0.factories.jakarta, \
 com.ibm.ws.webcontainer.servlet.3.1.jakarta, \
 com.ibm.ws.session.jakarta, \
 com.ibm.ws.webcontainer.jakarta, \
 com.ibm.ws.webcontainer.cors.jakarta, \
 com.ibm.ws.http.plugin.merge, \
 com.ibm.ws.webserver.plugin.runtime.jakarta, \
 com.ibm.ws.webserver.plugin.runtime.interfaces
-jars=com.ibm.ws.webserver.plugin.utility
-files=bin/tools/ws-webserverPluginutil.jar, \
 bin/pluginUtility; ibm.executable:=true; ibm.file.encoding:=ebcdic, \
 bin/pluginUtility.bat
kind=ga
edition=core
WLP-Activation-Type: parallel
