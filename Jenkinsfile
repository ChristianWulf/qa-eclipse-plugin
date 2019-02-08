#!groovy

ID = "kdt-jenkins"
UPDATE_SITE_URL = "sftp://repo@repo.se.internal/var/www/html/qa"

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
			sh '[ ! -d qa ] && mkdir qa'
			sh 'scp qa repo@repo.se.internal:/var/www/html/qa'
			sh 'cd ' + env.WORKSPACE + '; apache-maven-3.6.0/bin/mvn -X -s settings.xml -B package -Dkeystore=${key_file} -DupdatesiteUrl=' + UPDATE_SITE_URL
		}
	}
}

