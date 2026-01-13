
def call(String projectName, String projectKey , String projectVersion){
  
         withSonarQubeEnv(credentialsId: 'SONARQUBE_TOKEN', installationName: 'sonarqube-scanner') {
                script{
                    sh """
                    ${scannerHome}/bin/sonar-scanner \
                    -Dsonar.projectKey=${projectKey} \
                    -Dsonar.projectName="${projectName}" \
                    -Dsonar.projectVersion=${projectVersion} \
                    -Dsonar.exclusions="pipes/**"
                     """   
                        
                        }
            }
}