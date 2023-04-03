FROM ubuntu:lunar

WORKDIR /power

RUN apt -y update
RUN apt -y install ca-certificates
RUN apt -y install npm

COPY index.js package.json package-lock.json /power/
COPY api/ /power/api/

RUN npm install
COPY .env /power/
CMD npm run start
