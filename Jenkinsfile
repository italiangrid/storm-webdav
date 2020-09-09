#!/usr/bin/env groovy
@Library('sd')_
def kubeLabel = getKubeLabel()

pipeline {

  agent {
    kubernetes {
      label "${kubeLabel}"
      cloud 'Kube mwdevel'
      defaultContainer 'runner'
      inheritFrom 'ci-template'
    }
  }

  options {
    buildDiscarder(logRotator(numToKeepStr: '5'))
    timeout(time: 2, unit: 'HOURS')
  }
    
  triggers { cron('@daily') }

  stages {
    stage('build') {
      steps {
        sh 'mvn -B clean compile'
      }
    }
    
    stage('test') {
      steps {
        sh 'mvn -B clean test'
      }
      post {
        always {
          junit '**/target/surefire-reports/TEST-*.xml'
        }
      }
    }
    
    stage('analysis') {
      when{
        anyOf { branch 'master'; branch 'develop' }
        environment name: 'CHANGE_URL', value: ''
      }
      steps {
        script {
          def opts = '-Dmaven.test.failure.ignore -DfailIfNoTests=false'
          def checkstyle_opts = 'checkstyle:check -Dcheckstyle.config.location=google_checks.xml'
          sh "mvn clean compile -U ${opts} ${checkstyle_opts}"
        }
      }
    }
    
    stage('package') {
      steps {
        sh 'mvn -B -DskipTests=true clean package'
      }
    }
    
    stage('result') {
      steps {
        script { currentBuild.result = 'SUCCESS' }
      }
    }
  }
    
  post {
    failure {
     slackSend color: 'danger', message: "${env.JOB_NAME} - #${env.BUILD_NUMBER} Failure (<${env.BUILD_URL}|Open>)"
    }
    unstable {
      slackSend color: 'warning', message: "${env.JOB_NAME} - #${env.BUILD_NUMBER} Unstable (<${env.BUILD_URL}|Open>)"
    }
    changed {
      script{
        if('SUCCESS'.equals(currentBuild.result)) {
          slackSend color: 'good', message: "${env.JOB_NAME} - #${env.BUILD_NUMBER} Back to normal (<${env.BUILD_URL}|Open>)"
        }
      }
    }
  }  
}
