@echo off

echo.
echo.............................................
echo...                                       ...
echo... Remove Source Code Dokumentation for  ...
echo...                                       ...
echo...            C E R B E R U S            ...
echo.............................................
echo.

call doxygen\win32\remove_doxygen.bat
call javadoc\win32\remove_javadoc.bat


echo.
echo.............................................
echo...                                       ...
echo... Removed Source Code Dokumentation for ...
echo...                                       ...
echo...            C E R B E R U S            ...
echo.............................................
echo.

pause