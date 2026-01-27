set "bin=java -Dfile.encoding=utf-8 -classpath lib\commons-cli-1.10.0.jar;rubikAutoPlayer.jar ch.AutoPlayer"
::set "bin=rubikAutoPlayer\rubikAutoPlayer.exe"

::%bin% -h
::%bin% --rearView false
%bin% -backgroundImage %systemroot%\Web\Wallpaper\Windows\img0.jpg -rearViewBackgroundImage %systemroot%\Web\Wallpaper\Windows\img0.jpg --display true --autoTest 600000 -h
::%bin% --script "B' R  F' D  L  B  R' B' U  L2 B  L2 D  R2 U' B2 R2 F2 R' R F2 R2 B2 U R2 D' L2 B' L2 U' B R B' L' D' F R' B  B' R  F' D  L  B  R' B' U  L2 B  L2 D  R2 U' B2 R2 F2 R' R F2 R2 B2 U R2 D' L2 B' L2 U' B R B' L' D' F R' B  B' R  F' D  L  B  R' B' U  L2 B  L2 D  R2 U' B2 R2 F2 R' R F2 R2 B2 U R2 D' L2 B' L2 U' B R B' L' D' F R' B  B' R  F' D  L  B  R' B' U  L2 B  L2 D  R2 U' B2 R2 F2 R' R F2 R2 B2 U R2 D' L2 B' L2 U' B R B' L' D' F R' B  B' R  F' D  L  B  R' B' U  L2 B  L2 D  R2 U' B2 R2 F2 R' R F2 R2 B2 U R2 D' L2 B' L2 U' B R B' L' D' F R' B  B' R  F' D  L  B  R' B' U  L2 B  L2 D  R2 U' B2 R2 F2 R' R F2 R2 B2 U R2 D' L2 B' L2 U' B R B' L' D' F R' B  "
