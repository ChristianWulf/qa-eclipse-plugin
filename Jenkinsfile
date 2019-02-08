#!groovy

LOCAL_PATH = "/opt/se/qa-eclipse-plugin"
ID = "kdt-jenkins"
REPO_HOST = "repo.se.internal"

node {
	stage ('Checkout') {
		timeout(time: 3, unit: 'MINUTES') {	// typically finished in under 1 min.
			checkout scm
		}
	}

	stage ('Prepare') {
		sh 'cd ' + env.WORKSPACE + '; wget http://www.gutscheine.org/mirror/apache/maven/maven-3/3.6.0/binaries/apache-maven-3.6.0-bin.tar.gz ; tar -xzpf apache-maven-3.6.0-bin.tar.gz'
		sh 'cd ' + env.WORKSPACE + '; apache-maven-3.6.0/bin/mvn -s settings.xml -B clean'
	}

	stage ('Compile and Deploy') {
		withCredentials([file(credentialsId: ID, variable: 'key_file')]) {
			sh 'cd ' + env.WORKSPACE + '; apache-maven-3.6.0/bin/mvn -X -s settings.xml -B package -Dkeystore=${key_file} -Dupdatesite=repo@' + REPO_HOST + '/var/www/html'
		}
	}
}

