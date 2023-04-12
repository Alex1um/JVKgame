
rootProject.name = "testGame"
include("src:main:game")
findProject(":src:main:game")?.name = "game"
