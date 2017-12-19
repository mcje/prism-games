#!/bin/sh



rm -f examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
touch examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.0,gamma=0.0,eta=1.0,lambda=0.0,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.0,gamma=0.0,eta=1.0,lambda=0.5,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.0,gamma=0.0,eta=1.0,lambda=1.0,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.0,gamma=0.0,eta=1.0,lambda=1.5,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.0,gamma=0.0,eta=1.0,lambda=2.0,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.0,gamma=0.0,eta=1.0,lambda=2.5,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.0,gamma=0.0,eta=1.0,lambda=3.0,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.2,gamma=0.0,eta=1.0,lambda=0.0,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.2,gamma=0.0,eta=1.0,lambda=0.5,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.2,gamma=0.0,eta=1.0,lambda=1.0,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.2,gamma=0.0,eta=1.0,lambda=1.5,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.2,gamma=0.0,eta=1.0,lambda=2.0,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.2,gamma=0.0,eta=1.0,lambda=2.5,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.2,gamma=0.0,eta=1.0,lambda=3.0,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.4,gamma=0.0,eta=1.0,lambda=0.0,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.4,gamma=0.0,eta=1.0,lambda=0.5,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.4,gamma=0.0,eta=1.0,lambda=1.0,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.4,gamma=0.0,eta=1.0,lambda=1.5,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.4,gamma=0.0,eta=1.0,lambda=2.0,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.4,gamma=0.0,eta=1.0,lambda=2.5,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.4,gamma=0.0,eta=1.0,lambda=3.0,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.6,gamma=0.0,eta=1.0,lambda=0.0,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.6,gamma=0.0,eta=1.0,lambda=0.5,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.6,gamma=0.0,eta=1.0,lambda=1.0,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.6,gamma=0.0,eta=1.0,lambda=1.5,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.6,gamma=0.0,eta=1.0,lambda=2.0,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.6,gamma=0.0,eta=1.0,lambda=2.5,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.6,gamma=0.0,eta=1.0,lambda=3.0,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.8,gamma=0.0,eta=1.0,lambda=0.0,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.8,gamma=0.0,eta=1.0,lambda=0.5,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.8,gamma=0.0,eta=1.0,lambda=1.0,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.8,gamma=0.0,eta=1.0,lambda=1.5,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.8,gamma=0.0,eta=1.0,lambda=2.0,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.8,gamma=0.0,eta=1.0,lambda=2.5,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=0.8,gamma=0.0,eta=1.0,lambda=3.0,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=1.0,gamma=0.0,eta=1.0,lambda=0.0,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=1.0,gamma=0.0,eta=1.0,lambda=0.5,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=1.0,gamma=0.0,eta=1.0,lambda=1.0,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=1.0,gamma=0.0,eta=1.0,lambda=1.5,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=1.0,gamma=0.0,eta=1.0,lambda=2.0,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=1.0,gamma=0.0,eta=1.0,lambda=2.5,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
PRISM_JAVAMAXMEM=16g bin/prism examples/games/CDM/experiments/prob_choose_best/cdm5532.smg -ex -pctl '<<0,1>> Pmax=? [F all_committed&all_prefer_1]' -const Pexp=1.0,gamma=0.0,eta=1.0,lambda=3.0,Q1=1.0,Q2=0.5,Q3=0.25 >> examples/games/CDM/experiments/prob_choose_best/results/res_dtmc_lambda.txt
