final String restSslKeyStores = 'grails-app/rest/keystores'



eventPluginInstalled = {
    ant.mkdir(dir: restSslKeyStores)
}

eventCompileEnd = {
    if ((restSslKeyStores as File).exists()) {
        ant.copy(todir: "${classesDirPath}/") {
            fileset(dir: restSslKeyStores)
        }
    }
}
