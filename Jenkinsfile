pipeline {
    agent any

    environment {
        SONARQUBE = 'SonarQube' // nom du serveur SonarQube configuré dans Jenkins
        MAVEN_REPO_URL = 'https://mymavenrepo.com/repo/cEmjfkxugPlzLxXg1A2B/'
        MAVEN_CREDENTIALS_ID = 'maven-credentials' // si utilisé dans Jenkins
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

                echo "Génération des rapports Cucumber"
                bat 'gradlew.bat cucumberReports'
                archiveArtifacts 'build/reports/cucumber/html/**/*.html'
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
                bat 'gradlew.bat jar generateDocs'
                archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
                archiveArtifacts artifacts: 'build/docs/**', fingerprint: true
            }
        }

        // ===========================
        stage('Deploy') {
            steps {
                echo "Déploiement du Jar sur Maven repo"
                bat "gradlew.bat publish"
            }
        }

    }

    post {
        success {
            echo "Pipeline terminé avec succès: Notification par mail et Slack"

            // Envoi mail via Gradle plugin
            bat 'gradlew.bat sendMail'

            // Envoi Slack via Gradle plugin
            bat 'gradlew.bat postPublishedPluginToSlack'
        }

        failure {
            echo "Pipeline échoué: Notification par mail et Slack"

            // Mail de notification en cas d'échec
            mail to: EMAIL_RECIPIENTS,
                 subject: "Pipeline Failure: ${currentBuild.fullDisplayName}",
                 body: "Le pipeline a échoué à l'étape ${currentBuild.currentResult}."

            slackSend channel: SLACK_CHANNEL,
                      color: 'danger',
                      message: "Pipeline échoué: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
        }
    }
}
