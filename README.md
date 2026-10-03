# Regional Fund Allocator

How should a ministry split €100m across Greece's 13 regions? This project compares allocation rules, measures how concentrated each one is, stress-tests them against data error, and backtests them against what actually happened.

This is my answer for the final of the **Hellenic Statistics Olympiad 2026**. The task was to propose a funding rule for the trade sector (NACE Sector Ζ, wholesale and retail trade).   

![Productivity vs concentration](docs/frontier.png)

## What it does

| Stage | Question | Method |
|---|---|---|
| 1. Compare rules | Who gets what under each rule? | Value added share, employment share, 50/50 blend (my Olympiad model), blend with a minimum floor |
| 2. Sensitivity sweep | What does diversification cost? | Sweep the blend weight and the floor; trace concentration (HHI) against funding-weighted productivity |
| 3. Monte Carlo | Which results survive data error? | 10,000 runs with 5% normal noise on ELSTAT's estimates; 90% intervals and rank stability per region |
| 4. Backtest | Would any rule have picked the regions that grew? | Rules built on 2021 data only, scored on 2021 to 2023 growth with Spearman rank IC and a permutation test |

## Key findings

**1. The Olympiad model is highly concentrated.** Attica receives €57.55m. The allocation behaves like only 2.7 equally funded regions (HHI 0.364).

| Rule | Attica | North Aegean | HHI | Effective regions |
|---|---|---|---|---|
| Value added share | €65.36m | €0.58m | 0.453 | 2.2 |
| Employment share | €49.73m | €1.16m | 0.288 | 3.5 |
| 50/50 blend (Olympiad) | €57.55m | €0.87m | 0.364 | 2.7 |
| 50/50 blend + 2% floor | €44.58m | €2.65m | 0.234 | 4.3 |

**2. Diversification has a price.** Moving from the Olympiad model to a 2% floor raises effective regions from 2.7 to 4.3 but lowers funding-weighted productivity from €26.1k to €23.6k per worker, about 10%. Where the two levers overlap they sit on almost the same curve, but only the floor can diversify beyond the employment-only allocation.

**3. Only the top of the ranking is robust.** Under 5% data error, Attica, Central Macedonia and Crete keep their rank in 100% of simulations. Ranks 4 to 7 are close to a coin toss: Thessaly keeps 5th place in only 43% of runs, because the gaps between mid-sized regions are smaller than the data's own error.

**4. The backtest finds no reliable edge.** Built on 2021 data, every size-based rule lagged an equal split on value added growth by 8 to 13 points (largely a post-Covid tourism rebound in the islands) but slightly beat it on employment growth. No IC is significant (all p > 0.1). With 13 regions and one period, |IC| would need to exceed roughly 0.56 to reach 5% significance, so the honest conclusion is that this data cannot separate the rules.

**5. A data-quality catch.** ELSTAT's 2022 figures report *negative* Sector Ζ value added for Central Greece (−€676m) and the Ionian Islands (−€124m). The code refuses to compute shares or growth from a negative base instead of silently producing nonsense, which is why 2021 is the formation year for the backtest.

## Project structure

```
src/allocator/
├── Main.java                  runs all four stages
├── model/                     RegionData, Metric, Dataset, Allocation
├── strategy/                  AllocationStrategy (interface) and its implementations
├── analysis/                  SensitivitySweep, MonteCarloSimulator, Backtester, RankCorrelation
└── io/                        CsvLoader, CsvWriter
test/allocator/                24 JUnit 5 tests
data/sector_z_2021_2023.csv    ELSTAT Structural Business Statistics, Sector Ζ, 13 regions, 2021 to 2023
docs/frontier.png              chart above
```

Design notes:
- **Strategy pattern.** Every rule implements `AllocationStrategy`, so comparing, sweeping, simulating and backtesting all work on any rule without changes.
- **Invariants enforced.** `Allocation` rejects weights that are negative or do not sum to 1, so a faulty rule fails loudly.
- **Reproducible.** Monte Carlo and permutation tests use a fixed random seed.
- **No dependencies.** Plain Java 17+; JUnit 5 only for tests.

## How to run

**Eclipse:** import the project, then right-click `Main.java` → Run As → Java Application. Results are written to `results/` as CSV. To run the tests, add JUnit 5 to the build path and right-click `test` → Run As → JUnit Test.

**Command line** (macOS/Linux):
```
javac -d out $(find src -name "*.java")
java -cp out allocator.Main
```

## Limitations and next steps

- **Funding-weighted productivity is a proxy**, not a measured return on the money.
- **The 5% error is an assumption.** ELSTAT's published sampling errors would be better, and employment (from administrative records) probably deserves a smaller error than value added.
- **Normal noise can in principle go negative.** A lognormal perturbation would be cleaner.
- **One backtest period, 13 regions.** More years or finer regional data (NUTS-3) are needed for any statistical power.
- **Next:** a cap strategy (no region above X%) and an optimiser that maximises productivity subject to a concentration limit.

## Data

Hellenic Statistical Authority (ELSTAT), Structural Business Statistics 2021 to 2023, as provided for the second stage of the 9th Panhellenic Statistics Competition. Monetary values are in thousand euros.

---
*Errikos Skiadas*
