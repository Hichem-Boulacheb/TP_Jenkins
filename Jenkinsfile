pipeline {
    agent any

    tools {
        maven 'Maven-3.9.0' // Nom configuré dans Jenkins Global Tool Configuration
        jdk 'JDK-17' // Nom configuré dans Jenkins Global Tool Configuration
    }

    environment {
        SONAR_HOST_URL = 'http://localhost:9000' // URL de votre SonarQube
        SONAR_TOKEN = credentials('sonarqube-token') // ID du credential dans Jenkins
        MAVEN_REPO_CREDENTIALS = credentials('mymavenrepo-credentials') // ID du credential
        SLACK_CHANNEL = '#dev-notifications' // Canal Slack
        EMAIL_RECIPIENTS = 'dev-team@example.com' // Liste des emails
    }

    stages {
        stage('Test') {
            steps {
                script {
                    echo '========== Phase Test =========='

                    // 1. Lancement des tests unitaires
                    sh 'mvn clean test'

                    // 2. Archivage des résultats des tests unitaires
                    junit '**/target/surefire-reports/*.xml'

                    // 3. Génération des rapports de tests Cucumber
                    cucumber buildStatus: 'UNSTABLE',
                            reportTitle: 'Cucumber Report',
                            fileIncludePattern: '**/cucumber.json',
                            trendsLimit: 10
                }
            }
        }

        stage('Code Analysis') {
            steps {
                script {
                    echo '========== Phase Code Analysis =========='

                    withSonarQubeEnv('SonarQube') { // 'SonarQube' est le nom du serveur configuré dans Jenkins
                        sh """
                            mvn sonar:sonar \
                            -Dsonar.projectKey=your-project-key \
                            -Dsonar.host.url=${SONAR_HOST_URL} \
                            -Dsonar.login=${SONAR_TOKEN}
                        """
                    }
                }
            }
        }

        stage('Code Quality') {
            steps {
                script {
                    echo '========== Phase Code Quality =========='

                    timeout(time: 5, unit: 'MINUTES') {
                        waitForQualityGate abortPipeline: true
                    }
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    echo '========== Phase Build =========='

                    // 1. Génération du fichier Jar (skip tests car déjà exécutés)
                    sh 'mvn clean package -DskipTests'

                    // 2. Génération de la documentation
                    sh 'mvn javadoc:javadoc'

                    // 3. Archivage du fichier Jar et de la documentation
                    archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
                    archiveArtifacts artifacts: '**/target/site/apidocs/**', fingerprint: true
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    echo '========== Phase Deploy =========='

                    // Déploiement vers mymavenrepo.com
                    sh """
                        mvn deploy:deploy-file \
                        -DgroupId=com.example \
                        -DartifactId=your-artifact-id \
                        -Dversion=\${BUILD_NUMBER} \
                        -Dpackaging=jar \
                        -Dfile=target/*.jar \
                        -DrepositoryId=mymavenrepo \
                        -Durl=https://mymavenrepo.com/repo/your-repo-path
                    """
                }
            }
        }

        stage('Notification') {
            steps {
                script {
                    echo '========== Phase Notification =========='

                    // Notification par email
                    emailext(
                        subject: "✅ Déploiement réussi - Build #${BUILD_NUMBER}",
                        body: """
                            <h2>Déploiement réussi</h2>
                            <p><strong>Projet:</strong> ${JOB_NAME}</p>
                            <p><strong>Build:</strong> #${BUILD_NUMBER}</p>
                            <p><strong>Statut:</strong> SUCCESS</p>
                            <p><strong>URL:</strong> ${BUILD_URL}</p>
                        """,
                        to: "${EMAIL_RECIPIENTS}",
                        mimeType: 'text/html'
                    )

                    // Notification Slack
                    slackSend(
                        channel: "${SLACK_CHANNEL}",
                        color: 'good',
                        message: "✅ Déploiement réussi - ${JOB_NAME} #${BUILD_NUMBER} (<${BUILD_URL}|Voir les détails>)"
                    )
                }
            }
        }
    }

    post {
        failure {
            script {
                echo '========== Notification d\'échec =========='

                // Notification par email en cas d'échec
                emailext(
                    subject: "❌ Échec du pipeline - Build #${BUILD_NUMBER}",
                    body: """
                        <h2>Échec du pipeline</h2>
                        <p><strong>Projet:</strong> ${JOB_NAME}</p>
                        <p><strong>Build:</strong> #${BUILD_NUMBER}</p>
                        <p><strong>Statut:</strong> FAILURE</p>
                        <p><strong>Phase échouée:</strong> ${STAGE_NAME}</p>
                        <p><strong>URL:</strong> ${BUILD_URL}</p>
                        <p>Veuillez consulter les logs pour plus de détails.</p>
                    """,
                    to: "${EMAIL_RECIPIENTS}",
                    mimeType: 'text/html'
                )

                // Notification Slack en cas d'échec
                slackSend(
                    channel: "${SLACK_CHANNEL}",
                    color: 'danger',
                    message: "❌ Échec du pipeline - ${JOB_NAME} #${BUILD_NUMBER} - Phase: ${STAGE_NAME} (<${BUILD_URL}|Voir les détails>)"
                )
            }
        }

        unstable {
            script {
                // Notification en cas de build instable
                emailext(
                    subject: "⚠️ Build instable - Build #${BUILD_NUMBER}",
                    body: """
                        <h2>Build instable</h2>
                        <p><strong>Projet:</strong> ${JOB_NAME}</p>
                        <p><strong>Build:</strong> #${BUILD_NUMBER}</p>
                        <p><strong>Statut:</strong> UNSTABLE</p>
                        <p><strong>URL:</strong> ${BUILD_URL}</p>
                    """,
                    to: "${EMAIL_RECIPIENTS}",
                    mimeType: 'text/html'
                )

                slackSend(
                    channel: "${SLACK_CHANNEL}",
                    color: 'warning',
                    message: "⚠️ Build instable - ${JOB_NAME} #${BUILD_NUMBER} (<${BUILD_URL}|Voir les détails>)"
                )
            }
        }

        always {
            // Nettoyage de l'espace de travail
            cleanWs()
        }
    }
}