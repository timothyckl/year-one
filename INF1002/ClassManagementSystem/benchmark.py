#!/usr/bin/env python3
"""
high-precision benchmark script for cms operations.

this module provides comprehensive benchmarking capabilities for the course
management system (cms), measuring performance across different operations
and dataset sizes with microsecond precision timing.

usage:
    python3 benchmark.py

the script will automatically:
    1. validate the environment and dependencies
    2. run benchmarks for all configured operations
    3. write results to a csv file for analysis
    4. provide real-time progress feedback

author: P1-08 
"""

import csv
import os
import subprocess
import sys
import time
from dataclasses import dataclass
from enum import Enum
from pathlib import Path
from typing import Dict, List, Optional, Tuple


# constants - configuration
CMS_EXECUTABLE: str = "./build/main"
DATA_DIRECTORY: str = "./data"
OUTPUT_FILENAME: str = "perf-results.csv"
CSV_HEADER: List[str] = ["dataset", "operation", "time_s"]
DECIMAL_PRECISION: int = 6

# constants - dataset sizes to benchmark
DATASET_SIZES: List[int] = [100, 500, 1000]

# constants - timeout and retry settings
SUBPROCESS_TIMEOUT_SECONDS: int = 60
MAX_RETRY_ATTEMPTS: int = 3


class BenchmarkOperation(Enum):
    """enumeration of available benchmark operations."""

    SHOW_ALL = "SHOW_ALL"
    QUERY_WORST = "QUERY_WORST"
    SORT_MARK_ASC = "SORT_MARK_ASC"
    ADV_QUERY_3_FILTERS = "ADV_QUERY_3_FILTERS"


@dataclass
class BenchmarkResult:
    """
    represents the result of a single benchmark execution.

    attributes:
        dataset_size: number of records in the dataset
        operation: the benchmark operation that was executed
        elapsed_time: time taken in seconds
        success: whether the benchmark completed successfully
        error_message: optional error message if benchmark failed
    """

    dataset_size: int
    operation: str
    elapsed_time: float
    success: bool
    error_message: Optional[str] = None


class BenchmarkRunner:
    """
    manages the execution and reporting of cms benchmarks.

    this class handles all aspects of benchmark execution including
    environment validation, test case execution, result collection,
    and csv output generation.
    """

    def __init__(
        self,
        cms_executable: str = CMS_EXECUTABLE,
        data_directory: str = DATA_DIRECTORY,
        output_filename: str = OUTPUT_FILENAME
    ) -> None:
        """
        initialise the benchmark runner with configuration.

        args:
            cms_executable: path to the cms executable
            data_directory: directory containing test data files
            output_filename: output csv file for results

        raises:
            FileNotFoundError: if cms executable doesn't exist
            NotADirectoryError: if data directory doesn't exist
        """
        self.cms_executable = Path(cms_executable)
        self.data_directory = Path(data_directory)
        self.output_file = Path(output_filename)
        self.results: List[BenchmarkResult] = []

        self._validate_environment()

    def _validate_environment(self) -> None:
        """
        validate that all required files and directories exist.

        raises:
            FileNotFoundError: if cms executable is missing
            NotADirectoryError: if data directory is missing
            PermissionError: if cms executable is not executable
        """
        if not self.cms_executable.exists():
            raise FileNotFoundError(
                f"cms executable not found: {self.cms_executable}\n"
                f"please build the project first using 'make' or similar."
            )

        if not os.access(self.cms_executable, os.X_OK):
            raise PermissionError(
                f"cms executable is not executable: {self.cms_executable}\n"
                f"please run: chmod +x {self.cms_executable}"
            )

        if not self.data_directory.is_dir():
            raise NotADirectoryError(
                f"data directory not found: {self.data_directory}\n"
                f"please ensure test data files are available."
            )

    def _generate_input_script(
        self,
        operation: BenchmarkOperation,
        data_file: Path,
        record_count: int
    ) -> str:
        """
        generate the input script for a specific benchmark operation.

        args:
            operation: the benchmark operation to execute
            data_file: path to the data file to load
            record_count: number of records in the dataset

        returns:
            formatted input script as string

        raises:
            ValueError: if operation type is not recognised
        """
        # common header for all operations
        script_lines = [
            "OPEN",
            str(data_file),
        ]

        # operation-specific commands
        if operation == BenchmarkOperation.SHOW_ALL:
            script_lines.extend([
                "SHOW ALL",
            ])

        elif operation == BenchmarkOperation.QUERY_WORST:
            # query for non-existent id to test worst-case performance
            script_lines.extend([
                "QUERY",
                "9999999",
            ])

        elif operation == BenchmarkOperation.SORT_MARK_ASC:
            # sort by mark in ascending order
            script_lines.extend([
                "SORT",
                "2",  # sort by mark
                "A",  # ascending order
            ])

        elif operation == BenchmarkOperation.ADV_QUERY_3_FILTERS:
            # advanced query with three filters
            script_lines.extend([
                "ADV QUERY",
                "1",   # filter by id
                "Y",   # yes, apply this filter
                "2",   # filter by mark
                "Y",   # yes, apply this filter
                "3",   # filter by course
                "A",   # operator: all
                "CS",  # course prefix
                "1",   # comparison: >=
                "60",  # mark threshold
            ])

        else:
            raise ValueError(f"unrecognised operation: {operation}")

        # common footer
        script_lines.append("EXIT")

        return "\n".join(script_lines) + "\n"

    def _execute_benchmark(
        self,
        record_count: int,
        operation: BenchmarkOperation,
        input_script: str
    ) -> BenchmarkResult:
        """
        execute a single benchmark test case with timing.

        args:
            record_count: number of records in the dataset
            operation: the benchmark operation to execute
            input_script: formatted input commands for cms

        returns:
            benchmark result with timing and status information
        """
        try:
            # measure execution time with microsecond precision
            start_time = time.time()

            process_result = subprocess.run(
                [str(self.cms_executable)],
                input=input_script,
                text=True,
                stdout=subprocess.DEVNULL,
                stderr=subprocess.PIPE,
                timeout=SUBPROCESS_TIMEOUT_SECONDS,
                check=False  # handle return codes manually
            )

            elapsed_time = time.time() - start_time

            # check for execution errors
            if process_result.returncode != 0:
                error_msg = (
                    f"process returned exit code {process_result.returncode}"
                )
                if process_result.stderr:
                    error_msg += f": {process_result.stderr.strip()}"

                print(
                    f"warning: {operation.value} failed - {error_msg}",
                    file=sys.stderr
                )

                return BenchmarkResult(
                    dataset_size=record_count,
                    operation=operation.value,
                    elapsed_time=elapsed_time,
                    success=False,
                    error_message=error_msg
                )

            return BenchmarkResult(
                dataset_size=record_count,
                operation=operation.value,
                elapsed_time=elapsed_time,
                success=True
            )

        except subprocess.TimeoutExpired:
            error_msg = f"execution timed out after {SUBPROCESS_TIMEOUT_SECONDS}s"
            print(
                f"warning: {operation.value} - {error_msg}",
                file=sys.stderr
            )
            return BenchmarkResult(
                dataset_size=record_count,
                operation=operation.value,
                elapsed_time=SUBPROCESS_TIMEOUT_SECONDS,
                success=False,
                error_message=error_msg
            )

        except Exception as e:
            error_msg = f"unexpected error: {str(e)}"
            print(
                f"error: {operation.value} - {error_msg}",
                file=sys.stderr
            )
            return BenchmarkResult(
                dataset_size=record_count,
                operation=operation.value,
                elapsed_time=0.0,
                success=False,
                error_message=error_msg
            )

    def _validate_data_file(self, data_file: Path, record_count: int) -> bool:
        """
        validate that a data file exists and is readable.

        args:
            data_file: path to the data file
            record_count: expected number of records (for error messages)

        returns:
            true if file is valid, false otherwise
        """
        if not data_file.exists():
            print(
                f"warning: data file not found: {data_file}",
                file=sys.stderr
            )
            print(
                f"skipping all benchmarks for {record_count}-record dataset",
                file=sys.stderr
            )
            return False

        if not data_file.is_file():
            print(
                f"warning: {data_file} is not a regular file",
                file=sys.stderr
            )
            return False

        if not os.access(data_file, os.R_OK):
            print(
                f"warning: {data_file} is not readable",
                file=sys.stderr
            )
            return False

        return True

    def run_benchmark_suite(self) -> None:
        """
        execute the complete benchmark suite for all dataset sizes and operations.

        this method orchestrates the entire benchmarking process, including
        validation, execution, and result collection.
        """
        print("starting cms benchmark suite...")
        print(f"cms executable: {self.cms_executable}")
        print(f"data directory: {self.data_directory}")
        print(f"output file: {self.output_file}")
        print()

        # iterate through each dataset size
        for record_count in DATASET_SIZES:
            data_file = self.data_directory / f"{record_count}-records.txt"

            # validate data file before attempting benchmarks
            if not self._validate_data_file(data_file, record_count):
                continue

            print(f"benchmarking {record_count}-record dataset...")

            # run all benchmark operations for this dataset size
            for operation in BenchmarkOperation:
                input_script = self._generate_input_script(
                    operation,
                    data_file,
                    record_count
                )

                result = self._execute_benchmark(
                    record_count,
                    operation,
                    input_script
                )

                self.results.append(result)

                # provide real-time feedback
                if result.success:
                    print(
                        f"  {operation.value}: "
                        f"{result.elapsed_time:.{DECIMAL_PRECISION}f}s"
                    )
                else:
                    print(
                        f"  {operation.value}: failed - {result.error_message}",
                        file=sys.stderr
                    )

            print()  # blank line between dataset sizes

    def write_results_to_csv(self) -> None:
        """
        write benchmark results to csv file.

        only successful benchmark results are written to the output file.
        failed benchmarks are excluded to avoid skewing analysis.

        raises:
            IOError: if unable to write to output file
        """
        try:
            with open(self.output_file, 'w', newline='', encoding='utf-8') as csvfile:
                writer = csv.writer(csvfile)

                # write header
                writer.writerow(CSV_HEADER)

                # write successful results only
                successful_count = 0
                for result in self.results:
                    if result.success:
                        writer.writerow([
                            result.dataset_size,
                            result.operation,
                            f"{result.elapsed_time:.{DECIMAL_PRECISION}f}"
                        ])
                        successful_count += 1

                print(f"results written to {self.output_file}")
                print(
                    f"  total benchmarks: {len(self.results)}, "
                    f"successful: {successful_count}, "
                    f"failed: {len(self.results) - successful_count}"
                )

        except IOError as e:
            print(
                f"error: failed to write results to {self.output_file}: {e}",
                file=sys.stderr
            )
            raise

    def print_summary(self) -> None:
        """print a summary of benchmark results grouped by operation."""
        if not self.results:
            print("no benchmark results to summarise.")
            return

        print("\nbenchmark summary")
        print("=" * 60)

        # group results by operation
        operations: Dict[str, List[BenchmarkResult]] = {}
        for result in self.results:
            if result.success:
                if result.operation not in operations:
                    operations[result.operation] = []
                operations[result.operation].append(result)

        # print summary for each operation
        for operation_name in sorted(operations.keys()):
            results = operations[operation_name]
            print(f"\n{operation_name}:")

            for result in sorted(results, key=lambda r: r.dataset_size):
                print(
                    f"  {result.dataset_size:>3} records: "
                    f"{result.elapsed_time:.{DECIMAL_PRECISION}f}s"
                )

            # calculate and display growth rate if multiple dataset sizes
            if len(results) >= 2:
                results_sorted = sorted(results, key=lambda r: r.dataset_size)
                first = results_sorted[0]
                last = results_sorted[-1]

                time_ratio = last.elapsed_time / first.elapsed_time if first.elapsed_time > 0 else 0
                size_ratio = last.dataset_size / first.dataset_size

                print(
                    f"  scaling: {size_ratio:.1f}x data → "
                    f"{time_ratio:.2f}x time"
                )


def main() -> int:
    """
    main entry point for the benchmark script.

    returns:
        exit code (0 for success, 1 for failure)
    """
    try:
        # initialise benchmark runner
        runner = BenchmarkRunner(
            cms_executable=CMS_EXECUTABLE,
            data_directory=DATA_DIRECTORY,
            output_filename=OUTPUT_FILENAME
        )

        # execute benchmark suite
        runner.run_benchmark_suite()

        # write results to csv
        runner.write_results_to_csv()

        # display summary
        runner.print_summary()

        print("\nbenchmark complete.")
        return 0

    except (FileNotFoundError, NotADirectoryError, PermissionError) as e:
        print(f"error: {e}", file=sys.stderr)
        return 1

    except KeyboardInterrupt:
        print("\n\nbenchmark interrupted by user.", file=sys.stderr)
        return 130  # standard exit code for ctrl+c

    except Exception as e:
        print(f"error: unexpected error occurred: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        return 1


if __name__ == "__main__":
    sys.exit(main())
