# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Doric** is a type-safe column API for Apache Spark DataFrames that provides compile-time safety without sacrificing performance. It wraps Spark's Column API to catch errors at compile time rather than runtime, using functional programming patterns with Cats.

## 🚧 ACTIVE MIGRATION: Spark 4.0

**CRITICAL**: The project is currently migrating to Spark 4.0. This is a complex, step-by-step migration.

- **Goal**: Make Doric compatible with Spark 4.0 only (dropping all previous versions 2.4-3.5)
- **Strategy**: Incremental fixes - get things working piece by piece
- **Current state**: In progress - many features still broken
- **Branch**: feature/delete-pre-3.5-versions

### Migration Approach

1. Start with existing version-specific directories and consolidate them for 4.0
2. Fix compilation errors incrementally
3. Fix test failures one by one
4. Remove old version-specific code as we go
5. Update build configuration to only support Spark 4.0

### When Working on Code

- Assume we're targeting Spark 4.0 exclusively
- Old multi-version support code can be removed/simplified
- Focus on making individual features work rather than maintaining backward compatibility
- Check Spark 4.0 API changes when fixing compilation errors

## Build Commands

### Testing
```bash
# Run all tests (Spark 4.0.1 - default during migration)
sbt test

# Run a single test file
sbt "testOnly doric.syntax.ArrayColumnsSpec"

# Run with specific pattern
sbt "testOnly *ArrayColumns*"

# Run tests with coverage
sbt coverage test coverageReport
```

### Building
```bash
# Compile (targets Spark 4.0 by default)
sbt compile

# Build documentation
sbt +docs/mdoc

# Generate scaladoc
sbt doc
```

### Migration-Specific Commands
```bash
# Compile and see what breaks
sbt compile 2>&1 | tee compile-errors.log

# Run single test to fix incrementally
sbt "testOnly doric.syntax.NumericOperationsSpec"
```

### Code Quality
```bash
# Check code formatting (does not modify files)
sbt scalafmtCheckAll

# Auto-format all code
sbt scalafmtAll
```

## Architecture

### Source Directory Structure (Migration in Progress)

Previously, Doric supported multiple Spark versions using mounted directories. During migration:

- **Base implementation**: `core/src/main/scala/` - main code being updated for Spark 4.0
- **Legacy version-specific directories**: `spark_3.5_mount/`, `spark_4.0_mount/` - being consolidated
- **Legacy test directories**: Names like `spark_3.0_3.1_3.2_3.3_3.4_3.5_4.0/` - will be simplified to just version 4.0
- **Build configuration**: `build.sbt` still has multi-version logic but will be simplified to only Spark 4.0

The goal is to eliminate version-specific directories and have a single clean codebase for Spark 4.0.

### Core Type System

The type safety is built on these core abstractions:

1. **DoricColumn[T]**: The main abstraction representing a type-safe column
   - `NamedDoricColumn[T]`: Column with a name
   - `TransformationDoricColumn[T]`: Result of transformations
   - `LiteralDoricColumn[T]`: Literal values

2. **Doric[T]**: Type alias for `Kleisli[DoricValidated, Dataset[_], T]`
   - Uses Kleisli from Cats to represent computations that depend on a Dataset
   - Returns `DoricValidated[T]` which accumulates errors using `ValidatedNec`

3. **Error handling**: All operations return `Validated` to accumulate multiple errors instead of failing fast

### Package Structure

- **doric.syntax**: Column operations organized by type (ArrayColumns, StringColumns, NumericColumns, etc.)
- **doric.sem**: Semantic operations on DataFrames (TransformOps, JoinOps, CollectOps, SortingOps)
- **doric.types**: Type system for mapping Scala types to Spark DataTypes
- **org.apache.spark.sql.doric**: Extensions to Spark internals (requires package prefix for access)

### Key Design Patterns

1. **Type safety through evidence**: Functions require implicit `SparkType[T]` to ensure type compatibility
2. **Error accumulation**: Uses Cats `Validated` to collect all errors instead of fail-fast
3. **Kleisli pattern**: Computations are functions from Dataset to results, allowing composition
4. **Implicit conversions**: `syntax.All` trait provides implicits for ergonomic API

## Development Workflow

### Adding New Column Functions

1. Determine the appropriate file in `doric.syntax` (e.g., `ArrayColumns.scala` for array operations)
2. Add type-safe wrapper using `DoricColumn[T]` and require appropriate type evidence
3. Add tests in corresponding spec file (e.g., `ArrayColumnsSpec.scala`)
4. Only worry about Spark 4.0 compatibility - no version-specific logic needed

### Working with Spark Internals

Code in `org.apache.spark.sql.doric` package accesses Spark private APIs:
- `DoricUnresolvedFunction`: Wraps Spark's UnresolvedFunction
- `RelationalGroupedDatasetDoricInterface`: Provides access to grouping internals
- This requires the package prefix matching Spark's internal structure

### Testing Conventions

- Tests extend `DoricTestElements` which provides `SparkSessionTestWrapper`
- Use `shouldEqual` from `Equalities` for DataFrame comparisons
- Tests are organized to mirror main source structure
- Version-specific tests live in appropriately named directories

## Commit Conventions

Follow [Conventional Commits](https://www.conventionalcommits.org/) specification:
- `feat:` for new features
- `fix:` for bug fixes
- `refactor:` for refactoring
- `test:` for test changes
- `docs:` for documentation

## CI/CD Notes

GitHub Actions runs:
1. Tests against Spark 4.0 (will be simplified from previous multi-version matrix)
2. `scalafmt` checks (fails if code is not formatted)
3. `mdoc` documentation build
4. Coverage reports to Codecov
5. ScalaDoc generation

All checks must pass before merge. The project uses squash merging.

**During migration**: Some CI checks may be temporarily failing - focus on incremental progress.