def call(String projectName, String projectKey, String projectVersion) {

    script {
        def scannerHome = tool 'sonarqube-scanner'

        withSonarQubeEnv(credentialsId: 'SONARQUBE-TOKEN',
                         installationName: 'sonarqube-scanner') {

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
