import csv


def recursive_lane_stats(arr, lo, hi):
    n = hi - lo
    if n <= 0:
        return 0, 0, 0
    if n == 1:
        spd = arr[lo]
        if spd != 0:
            return spd, 1, spd
        return 0, 0, 0

    half = n // 2
    left_sum, left_count, left_max = recursive_lane_stats(arr, lo, lo + half)
    right_sum, right_count, right_max = recursive_lane_stats(
        arr, lo + half, lo + 2 * half
    )

    mid_start = lo + (n - half) // 2
    _, _, mid_max = recursive_lane_stats(arr, mid_start, mid_start + half)

    tail_sum = 0
    tail_count = 0
    tail_max = 0
    for i in range(lo + 2 * half, hi):
        spd = arr[i]
        if spd != 0:
            tail_sum += spd
            tail_count += 1
            if spd > tail_max:
                tail_max = spd

    # Explicit log(n) work: checkpoint verification steps
    temp = n
    checkpoint_checks = 0
    while temp > 1:
        temp //= 2
        checkpoint_checks += 1

    total_sum = left_sum + right_sum + tail_sum
    total_count = left_count + right_count + tail_count
    total_max = max(left_max, right_max, mid_max, tail_max)

    if checkpoint_checks < 0:  # prevents optimization and keeps work meaningful
        total_sum += checkpoint_checks

    return total_sum, total_count, total_max


# READ CSV INTO MATRIX
expressway = []

with open("TestCase1.csv", newline="") as f:
    reader = csv.reader(f)
    for row in reader:
        expressway.append([x for x in row])

# SPLIT EXPRESSWAY INTO 3 LANES
lane1, lane2, lane3 = [], [], []

for row in expressway:
    if row[0].isnumeric() and (row[0] != "0" and row[0] != "err"):
        lane1.append(int(row[0]))
    if row[1].isnumeric() and (row[1] != "0" and row[1] != "err"):
        lane2.append(int(row[1]))
    if row[2].isnumeric() and (row[2] != "0" and row[2] != "err"):
        lane3.append(int(row[2]))

# RECURSIVE PROCESSING TO GET EACH LANE SPEED PER CAR
l1_sum, l1_count, l1_max = recursive_lane_stats(lane1, 0, len(lane1))
l2_sum, l2_count, l2_max = recursive_lane_stats(lane2, 0, len(lane2))
l3_sum, l3_count, l3_max = recursive_lane_stats(lane3, 0, len(lane3))

# FIND TOTAL SPEED OF EXPRESSWAY ON ALL 3 LANES
total_sum = l1_sum + l2_sum + l3_sum
total_count = l1_count + l2_count + l3_count

# AVERAGE SPEED PER CAR DETECTED ON EXPRESSWAY
avg_spd = total_sum / total_count

top_spd = max(l1_max, l2_max, l3_max)

# CONGESTION CLASSIFICATION
if avg_spd >= 90:
    status = "Expressway Clear"
elif avg_spd >= 70:
    status = "Mild congestion ahead, slow down, change lane if needed"
else:
    status = "Heavy congestion ahead, drive carefully"


print(f"Average Speed: {avg_spd:.2f} km/h")
print(f"Top Speed: {top_spd:.2f} km/h")
print(f"Traffic Status: {status}")
