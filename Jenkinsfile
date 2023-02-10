#!/usr/bin/env groovy

pipeline {
  agent any

  environment {
    ID = "kdt-jenkins"
    UPDATE_SITE_URL = "sftp://repo@repo.se.internal/qa"
    DESTINATION = 'snapshot'
  }

  stages {
    stage ('Checkout') {
      steps {
        timeout(time: 3, unit: 'MINUTES') {  // typically finished in under 1 min.
          checkout scm
        }
      }
    }
    stage('Main') {
      agent {
        docker {
          image "prefec2/jdk11-maven-363-gradle671"
          alwaysPull false
        }
      }
      stages {
        stage('Build') {
          steps {
            sh 'mvn -Dmaven.repo.local=${WORKSPACE}/ws-repo --batch-mode compile'
          }
        }
        stage('Test') {
          steps {
            sh 'mvn -Dmaven.repo.local=${WORKSPACE}/ws-repo --batch-mode test'
          }
        }
        stage('Check') {
          steps {
            sh 'cd bundles ; mvn -Dmaven.repo.local=${WORKSPACE}/ws-repo --batch-mode compile checkstyle:checkstyle pmd:pmd -Dworkspace=' + env.WORKSPACE // spotbugs:spotbugs
          }
          post {
            always {
              recordIssues enabledForFailure: true, tools: [mavenConsole(), java(), javaDoc()]
              recordIssues enabledForFailure: true, tool: checkStyle()
      //          recordIssues enabledForFailure: true, tool: spotBugs()
              recordIssues enabledForFailure: true, tool: pmdParser()
            }
          }
        }
        stage('Package') {
          steps {
            sh 'mvn -Dmaven.repo.local=${WORKSPACE}/ws-repo --batch-mode package'
          }
        }
        stage ('Update Repository') {
          environment {
            KEYSTORE = credentials('kieker-irl-key')
          }
          when {
            branch 'master'
          }
          steps {
            sh '/usr/bin/sftp -i ${KEYSTORE} -o User=repo -o StrictHostKeyChecking=no -b ${WORKSPACE}/upload.sftp ${UPDATE_SITE_URL}/${DESTINATION}'
          }
        }
      }
    }
  }
}
