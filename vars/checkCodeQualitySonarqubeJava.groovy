def call(String projectName, String projectKey, String projectVersion) {
    def scannerHome = tool 'sonarqube-scanner'

    withSonarQubeEnv(
        credentialsId: 'SONARQUBE-TOKEN',
        installationName: 'sonarqube-scanner'
    ) {
        sh """
            ${scannerHome}/bin/sonar-scanner \
            -Dsonar.projectKey=${projectKey} \
            -Dsonar.projectName="${projectName}" \
            -Dsonar.projectVersion=${projectVersion} \
            -Dsonar.sources=src/main/java \
            -Dsonar.java.binaries=build/classes \
            -Dsonar.exclusions=**/test/**,**/build/**
        """
    }
}