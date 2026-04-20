FROM public.ecr.aws/amazoncorretto/amazoncorretto:8

LABEL maintainer="xyzsince2014 <xyzsince2014@gmail.com>"

RUN amazon-linux-extras install tomcat9
RUN yum update -y
RUN yum install -y tomcat-webapps tomcat-admin-webapps vim fileutils

ENV TZ='Asia/Tokyo'

RUN echo 'alias ls="ls --color"' >> ~/.bashrc

COPY tomcat/conf/ /usr/share/tomcat/conf/
COPY tomcat/webapps/ROOT.war /usr/share/tomcat/webapps/ROOT.war

RUN rm -r /usr/share/tomcat/webapps/ROOT/
RUN rm -r /usr/share/tomcat/webapps/examples/
RUN rm -r /usr/share/tomcat/webapps/host-manager/
RUN rm -r /usr/share/tomcat/webapps/manager/
RUN rm -r /usr/share/tomcat/webapps/sample/

EXPOSE 8080

# start Tomcat directly in foreground
CMD ["/usr/libexec/tomcat/server", "start"]
