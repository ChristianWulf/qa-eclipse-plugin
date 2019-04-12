ID = "kdt-jenkins"
UPDATE_SITE_URL = "sftp://repo@repo.se.internal/var/www/html/qa"

pipeline {
	agent 'any'

	stages {
		stage ('Checkout') {
			steps {
				timeout(time: 3, unit: 'MINUTES') {	// typically finished in under 1 min.
					checkout scm
				}
			}
		}

		stage('Build') {
			steps {
				sh 'mvn --batch-mode compile'
			}
		}

		stage('test') {
			steps {
				sh 'mvn --batch-mode test'
			}
		}

		stage('Check') {
			steps {
				sh 'cd bundles ; mvn --batch-mode compile checkstyle:checkstyle -Dworkspace=${env.WORKSPACE}' // pmd:pmd spotbugs:spotbugs
			}
			post {
            			always {
					recordIssues enabledForFailure: true, tools: [mavenConsole(), java(), javaDoc()]
					recordIssues enabledForFailure: true, tool: checkStyle()
//					recordIssues enabledForFailure: true, tool: spotBugs()
//					recordIssues enabledForFailure: true, tool: pmdParser()
            			}
          		}
		}

		stage('Package') {
			steps {
				sh 'mvn --batch-mode package'
			}
		}

		stage ('Deploy') {
			when{
				branch 'master'
			}
			steps {
				withCredentials([file(credentialsId: ID, variable: 'key_file')]) {
					sh 'mvn -X -s settings.xml -B install -Dkeystore=${key_file} -DupdateSiteUrl=' + UPDATE_SITE_URL
				}
			}
		}
	}
}

