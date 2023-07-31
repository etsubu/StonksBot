FROM openjdk:11-jre-slim-buster
ARG appname
ARG AWS_DEFAULT_REGION
ARG AWS_ACCESS_KEY_ID
ARG AWS_SECRET_ACCESS_KEY
# Create user and configure aws access keys
RUN useradd -ms /bin/bash $appname \
&&  mkdir -p /home/${appname}/.aws \
&& printf "[default]\naws_access_key_id=${AWS_ACCESS_KEY_ID}\naws_secret_access_key=${AWS_SECRET_ACCESS_KEY}" > /home/$appname/.aws/credentials \
&& printf "[default]\nregion=${AWS_DEFAULT_REGION}\noutput=json" > /home/$appname/.aws/config \
&& chown -R ${appname}:${appname} /home/${appname}
# Install and update packages
RUN apt update && apt install curl unzip -y && apt upgrade -y && apt autoremove -y \
&& curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" \
&& unzip awscliv2.zip \
&& ./aws/install \
&& rm -rf aws
# Create cronjob for syncing configs from s3
COPY cronjob.sh /etc/cron.d/stonksbot
RUN chown root:root /etc/cron.d/stonksbot \
&& chmod 644 /etc/cron.d/stonksbot
# Create directories and start script
RUN mkdir -p /opt/application/ && mkdir -p /var/log/${appname} \
COPY start.sh /opt/application/start.sh
RUN chmod 744 /opt/application/start.sh
# Copy application
COPY app.jar /opt/application/${appname}.jar
# Set folder and application permissions
RUN chown -R ${appname}:${appname} /opt/application \
&& chown -R ${appname}:${appname} /var/log/${appname} \
&& chmod 644 /opt/application/${appname}.jar
# Default user to application
USER $appname
#Workdir
WORKDIR /home/${appname}
#Entrypoint
CMD ["/opt/application/start.sh"]
