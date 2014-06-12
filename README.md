OWAEmailWeb
===========

Jenkins plugin to send email with Microsoft Outlook Web, through web interface.

The objetive of this plugin it's to solve the problem of send email's in a network where the smtp 
or pop protocol is disabled or blocked by a firewall.


Features:
* Send email by http protocol
* Use firewall jenkis settings
* Configure the url of OWA, username, password and inbox
* Multiple templates of email to send
* Use Token Macro Plugin (https://wiki.jenkins-ci.org/display/JENKINS/Token+Macro+Plugin)
* Show in log if the email is sent or not.
* A post-build plugin
* Html/Text body content
* Multiple cc/cco

Installation
-----------
Download the sources, compile with maven and install by the plugin's admin panel of Jenkis.


ScreenShot
-----------

<b>Jenkins admin panel configuration</b>
![ScreenShot](https://raw.github.com/cedillomarcos/owawebemail-plugin/master/screenshots/image01.png)

<b>Job plugin configuration</b>
![ScreenShot](https://raw.github.com/cedillomarcos/owawebemail-plugin/master/screenshots/image02.png)

