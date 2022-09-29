AI体考项目
切换为"Project"目录组织方式， 在app/src/main/java/com/example/mediapipeposetracking 包下进行开发。
MainActivity是主程序，包含Pose Detection solution 33个关键点的获取以及 各个项目的调用方式。
  1. 其中onCreate函数中包含四个按钮控件，点击事件响应的代码已经写好，只需要补充对应的项目调用。 
  2. 其中poseDetection(String project)函数用于获取关键点，并根据输入字符串进行相关函数的调用。 
  3. 已有包"obliquePullUpsProjects"是斜身引体项目的具体代码，其他项目的增加也需要先添加相应的包，再添加相关代码。
  4. 在MainActivity中包含必要的、公用的成员变量，例如MediaPipe所需变量、用于显示和计时的控件（Button、TextView、Timer、Handler等），以及所需人体关键点
  5. 后续协同开发只需要增加、修改自己项目包内的代码，push到主分支即可。

前置工作：
1.mediapipe相关环境的搭建和aar包的编译

