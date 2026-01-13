https://api.telegram.org/bot7873147150:AAGVJ-bpejW4O0XS9FhLQmwEr5Wk-VK89-Y/getUpdates

@Library("telegrame_notification_share_library@main") _
pipeline {
    agent any
    environment{
      CHAT_ID="1177908131"
      CHAT_TOKEN="7873147150:AAGVJ-bpejW4O0XS9FhLQmwEr5Wk-VK89-Y"
    }

    stages {
        stage('Send Message') {
            steps {
                script{
                  def message = """
                  Your alert is ready to use
                  """
                  sendTelegrameMessage("${message}","${CHAT_TOKEN}","${CHAT_ID}")
                }
            }
        }
    }
}
