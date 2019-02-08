#!groovy

LOCAL_PATH = "/opt/se/qa-eclipse-plugin"
KDT_ID = "se-jenkins"
REPO_HOST = "repo.se.internal"

node {
	stage ('Checkout') {
		timeout(time: 3, unit: 'MINUTES') {	// typically finished in under 1 min.
			checkout scm
		}
	}

	stage ('Prepare') {
		sh 'mvn -version ; ls /var/lib/jenkins/tools/'
		sh 'cd ' + env.WORKSPACE + '; mvn -s settings.xml -B clean'
	}

	stage ('Compile and Deploy') {
		withCredentials([file(credentialsId: KDT_ID, variable: 'key_file')]) {
			sh 'cd ' + env.WORKSPACE + '; mvn -X -s settings.xml -B package -Dkeystore=${key_file} -Dupdatesite=repo@' + REPO_HOST + '/var/www/html'
		}
	}
}

