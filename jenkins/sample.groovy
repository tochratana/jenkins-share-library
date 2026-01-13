pipeline {
    agent any

    stages {
        stage('Clone Code') {
            steps {
                git url: 'https://github.com/tochratana/deploy', branch: 'main'
            }
        }

        stage("Scan with sonarqube "){
            environment{
                scannerHome= tool 'sonarqube-scanner' // name tool that we have config it in tool in jenkins
            }
            steps{
              withSonarQubeEnv(credentialsId: 'SONARQUBE-TOKEN', installationName: 'sonarqube-scanner') {
                  script{
                  def projectKey = 'next-testing-pipeline' 
                  def projectName = 'next-testing-pipeline'  // Replace with your project name, project name don't have space
                  def projectVersion = '1.0.0'  // Replace with your project version
                          // sh "${scannerHome}/bin/sonar-scanner"
                  sh """
                  ${scannerHome}/bin/sonar-scanner \
                  -Dsonar.projectKey=${projectKey} \
                  -Dsonar.projectName="${projectName}" \
                  -Dsonar.projectVersion=${projectVersion} \
                   """   
                          
                }
              }
            }
        }
        stage("Check Quality Gate"){
            steps{
                script{
                    timeout(time: 2, unit: 'MINUTES'){
                    def qg = waitForQualityGate()
                    if ( qg.status != 'OK'){
                        sh """
                        echo " No need to build since you QG is failed "
                        """
                        currentBuild.result='FAILURE'
                        return
                    }else {
                         currentBuild.result='SUCCESS'
                    }
                  }
                }
            }
    }
}
}
