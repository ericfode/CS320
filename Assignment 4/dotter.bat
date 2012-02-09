@echo off
echo Starting dotter

for %%v in (*.dot) do dot -Tpdf -o %%~nv.pdf %%v