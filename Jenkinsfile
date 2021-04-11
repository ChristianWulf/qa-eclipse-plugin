pipeline {
	agent {
        docker {
          image 'maven:3.6.3-openjdk-14'
          alwaysPull true
        }
      }

	environment {
		KEYSTORE = credentials('kdt-jenkins-key')
		UPDATE_SITE_URL = "sftp://repo@repo.se.internal/var/www/html/qa"
	}

	stages {
		stage('Build') {
			steps {
				sh 'mvn --batch-mode -Dmaven.repo.local=maven compile'
			}
		}
		stage('Test') {
			steps {
				sh 'mvn --batch-mode -Dmaven.repo.local=maven test'
			}
		}
		stage('Check') {
			steps {
				sh 'mvn --batch-mode -Dmaven.repo.local=maven package checkstyle:checkstyle pmd:pmd -Dworkspace=' + env.WORKSPACE // spotbugs:spotbugs
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
				sh 'mvn --batch-mode -Dmaven.repo.local=maven package'
			}
		}
		stage ('Update Repository') {
			when {
				branch 'master'
			}
			steps {
				sh 'mvn -Dmaven.repo.local=maven --settings settings.xml --batch-mode install -Dkeystore=${KEYSTORE} -Dupdate-site-url=${UPDATE_SITE_URL}'
			}
		}
	}
}
