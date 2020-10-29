// This Jenkinsfile is used by Jenkins to run the Wikidata step of Reactome's release.
// It requires that the ConfirmReleaseConfigs step has been run successfully before it can be run.

import groovy.json.JsonSlurper
import org.reactome.release.jenkins.utilities.Utilities

// Shared library maintained at 'release-jenkins-utils' repository.
def utils = new Utilities()

pipeline {
	agent any
	
	environment {
	    EXPORTER_OUTPUT_FOLDER = getOutputFolderPath()
	}
	
	stages{
	    stage('Checkout') {
            steps {
                // Get some code from a GitHub repository
                git branch: 'feature/jenkins-integration', url: 'https://github.com/reactome/wikidata-exporter.git'
            }
        }
        stage('Setup: Clone Wikidata Bot repository') {
            steps{
                script{
                    utils.cloneOrUpdateLocalRepo("r-wikidata-bot")
                    dir("r-wikidata-bot") {
                        sh "git checkout origin/feature-revamp-bot"
                        sh "python3 -m pipenv install --ignore-pipfile"
                    }
                }
            }
        }
        // This stage builds the jar file using maven.
		stage('Setup: Build jar files'){
			steps{
				script{
					utils.buildJarFile()
				}
			}
		}
		stage('Main: Run Wikidata-Exporter'){
			steps{
				script{
					sh "mkdir -p ${env.EXPORTER_OUTPUT_FOLDER}"
					withCredentials([usernamePassword(credentialsId: 'neo4jUsernamePassword', passwordVariable: 'pass', usernameVariable: 'user')]){
						sh "java -Xmx${env.JAVA_MEM_MAX}m -jar target/wikidata-exporter-jar-with-dependencies.jar --user ${user} --password ${pass} --outputdirectory ${env.EXPORTER_OUTPUT_FOLDER}"
					}
				}
			}
		}
		stage('Main: Run R-Wikidata-Bot') {
		    steps{
		        script{
        		    dir("r-wikidata-bot") {
        		        sh "python3 -m pipenv run python bot.py -d ${env.EXPORTER_OUTPUT_FOLDER} -rnews https://reactome.org/about/news/sample_news_link -rday 11 -rmonth June -ryear 2020"
        		    }
		        }
		    }
		}
	}
}

def getOutputFolderPath() {
    return pwd() + "/data/"
}
