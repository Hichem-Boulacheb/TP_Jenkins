pipeline {
    agent any

    environment {
        SONARQUBE = 'sonar' // nom du serveur SonarQube configuré dans Jenkins
        MAVEN_REPO_URL = 'https://mymavenrepo.com/repo/cEmjfkxugPlzLxXg1A2B/'
        SLACK_CHANNEL = '#dev-team'
        EMAIL_RECIPIENTS = 'lh_boulacheb@esi.dz'
    }

    options {
        skipStagesAfterUnstable()
    }

    stages {

        // ===========================
        stage('Test') {
            steps {
                echo "Phase Test: Lancement des tests unitaires"
                bat 'gradlew.bat clean test'

                echo "Archivage des résultats des tests unitaires"
                junit 'build/test-results/test/*.xml'
            }
        }

        // ===========================
        stage('Code Analysis') {
            steps {
                echo "Analyse du code avec SonarQube"
                withSonarQubeEnv(SONARQUBE) {
                    bat 'gradlew.bat sonarqube'
                }
            }
        }

        // ===========================
        stage('Code Quality') {
            steps {
                echo "Vérification des Quality Gates"
                timeout(time: 2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        // ===========================
        stage('Build') {
            steps {
                echo "Génération du Jar et de la documentation"
                bat 'gradlew.bat jar javadoc'
                archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
                archiveArtifacts artifacts: 'build/docs/javadoc/**', fingerprint: true, allowEmptyArchive: true
            }
        }

        // ===========================
        stage('Deploy') {
            steps {
                echo "Déploiement du Jar sur Maven repo"
                bat 'gradlew.bat publish'
            }
        }

    }

    post {
        always {
            echo "Nettoyage et archivage des artefacts"
            // Pas de rapport Cucumber dans ce projet
        }

        success {
            echo "Pipeline terminé avec succès: Notification par mail et Slack"

            script {
                try {
                    // Envoi mail
                    emailext(
                        to: "${EMAIL_RECIPIENTS}",
                        subject: "✅ Pipeline Success: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        body: """
                            <h2>Pipeline exécuté avec succès</h2>
                            <p><strong>Projet:</strong> ${env.JOB_NAME}</p>
                            <p><strong>Build:</strong> #${env.BUILD_NUMBER}</p>
                            <p><strong>Statut:</strong> SUCCESS</p>
                            <p><strong>URL:</strong> ${env.BUILD_URL}</p>
                            <p>Le déploiement a été effectué avec succès.</p>
                        """,
                        mimeType: 'text/html'
                    )

                    // Envoi Slack
                    slackSend(
                        channel: "${SLACK_CHANNEL}",
                        color: 'good',
                        message: "✅ Pipeline réussi: ${env.JOB_NAME} #${env.BUILD_NUMBER} (<${env.BUILD_URL}|Voir les détails>)"
                    )
                } catch (Exception e) {
                    echo "Erreur lors de l'envoi des notifications de succès: ${e.message}"
                }
            }
        }

        failure {
            echo "Pipeline échoué: Notification par mail et Slack"

            script {
                try {
                    // Mail de notification en cas d'échec
                    emailext(
                        to: "${EMAIL_RECIPIENTS}",
                        subject: "❌ Pipeline Failure: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        body: """
                            <h2>Pipeline échoué</h2>
                            <p><strong>Projet:</strong> ${env.JOB_NAME}</p>
                            <p><strong>Build:</strong> #${env.BUILD_NUMBER}</p>
                            <p><strong>Statut:</strong> ${currentBuild.currentResult}</p>
                            <p><strong>URL:</strong> ${env.BUILD_URL}</p>
                            <p>Veuillez consulter les logs pour plus de détails.</p>
                        """,
                        mimeType: 'text/html'
                    )

                    // Slack de notification en cas d'échec
                    slackSend(
                        channel: "${SLACK_CHANNEL}",
                        color: 'danger',
                        message: "❌ Pipeline échoué: ${env.JOB_NAME} #${env.BUILD_NUMBER} (<${env.BUILD_URL}|Voir les détails>)"
                    )
                } catch (Exception e) {
                    echo "Erreur lors de l'envoi des notifications d'échec: ${e.message}"
                }
            }
        }

        unstable {
            echo "Pipeline instable: Notification par mail et Slack"

            script {
                try {
                    emailext(
                        to: "${EMAIL_RECIPIENTS}",
                        subject: "⚠️ Pipeline Unstable: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        body: """
                            <h2>Pipeline instable</h2>
                            <p><strong>Projet:</strong> ${env.JOB_NAME}</p>
                            <p><strong>Build:</strong> #${env.BUILD_NUMBER}</p>
                            <p><strong>Statut:</strong> UNSTABLE</p>
                            <p><strong>URL:</strong> ${env.BUILD_URL}</p>
                        """,
                        mimeType: 'text/html'
                    )

                    slackSend(
                        channel: "${SLACK_CHANNEL}",
                        color: 'warning',
                        message: "⚠️ Pipeline instable: ${env.JOB_NAME} #${env.BUILD_NUMBER} (<${env.BUILD_URL}|Voir les détails>)"
                    )
                } catch (Exception e) {
                    echo "Erreur lors de l'envoi des notifications d'instabilité: ${e.message}"
                }
            }
        }
    }
}