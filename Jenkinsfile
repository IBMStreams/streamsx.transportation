pipeline {
  agent { label 'streamsx_public' }
  stages {
    stage('Build') {
      steps {
        sh 'ant'
      }
    }
  }
  post {
    always {
      publishHTML (target: [
          reportName: 'SPLDOC',
          reportDir: 'com.ibm.streamsx.transportation/doc',
          reportFiles: 'index.html',
          keepAll: false,
          alwaysLinkToLastBuild: true,
          allowMissing: true
      ])
    }
  }
}
