class Node:
    def __init__(self, data, nxt=None):
        self.data = data
        self.next = nxt


class O1LinkedList:

    def __init__(self):
        self.head = None
        self.idx = []  # Auxiliary index for O(1) access

    @property
    def size(self):
        return len(self.idx)

    # O(1) access
    def get(self, position):
        if position < 0 or position >= self.size:
            raise IndexError("Invalid position")
        return self.idx[position].data

    def get_node(self, position):
        if position < 0 or position >= self.size:
            raise IndexError("Invalid position")
        return self.idx[position]

    # O(1) updates given a handle
    def insert_at_head(self, data):
        new_node = Node(data, self.head)
        self.head = new_node
        # Keep idx consistent for O(1) access
        self.idx.insert(0, new_node)

    def insert_after(self, node, data):
        new_node = Node(data, node.next)
        node.next = new_node
        return new_node

    def remove_at_head(self):
        if self.head is None:
            raise IndexError("List is empty")
        removed = self.head
        self.head = removed.next
        self.idx.pop(0)
        return removed.data

    def remove_after(self, node):
        if node.next is None:
            raise IndexError("Nothing to remove at that location")
        removed = node.next
        node.next = removed.next
        return removed.data

    def insert(self, position, data):
        if position < 0 or position > self.size:
            raise IndexError("Invalid position")

        if position == 0:
            self.insert_at_head(data)
            return

        prev = self.get_node(position - 1)
        new_node = self.insert_after(prev, data)

        # Maintain idx so future get_node() works
        self.idx.insert(position, new_node)

    def remove(self, position):

        if position < 0 or position >= self.size:
            raise IndexError("Invalid position")

        if position == 0:
            return self.remove_at_head()

        prev = self.get_node(position - 1)
        removed_data = self.remove_after(prev)

        # Maintain idx so future get_node() works
        self.idx.pop(position)
        return removed_data

    # Display
    def display_with_indices(self):
        if self.size == 0:
            print("List: [Empty]")
            return

        curr = self.head
        out = []
        i = 0
        while curr:
            out.append(f"[{i}]{curr.data}")
            curr = curr.next
            i += 1
        print("List: " + " -> ".join(out))


def populate_list(ll):
    """Allow user to populate the list with initial data"""
    print("\n" + "=" * 60)
    print("  POPULATE LIST")
    print("=" * 60)
    print("Enter initial elements for the list.")
    print("You can enter multiple elements separated by commas,")
    print("or press Enter to skip and start with an empty list.")
    print("=" * 60)

    user_input = input("\nEnter elements (e.g., A,B,C or 1,2,3): ").strip()

    if not user_input:
        print("\nStarting with an empty list.")
        return

    # Split by comma and strip whitespace
    elements = [elem.strip() for elem in user_input.split(",") if elem.strip()]

    # Insert each element at the end of the list
    for elem in elements:
        ll.insert(ll.size, elem)

    print(f"\n[SUCCESS] Added {len(elements)} element(s) to the list.")
    ll.display_with_indices()
    print(f"Initial size: {ll.size}")
    input("\nPress Enter to continue to main menu...")


def print_menu():
    print("\n" + "=" * 60)
    print("  LINKED LIST - INTERACTIVE TESTER")
    print("=" * 60)
    print("1. Get element")
    print("2. Insert element")
    print("3. Remove element")
    print("4. Display list")
    print("5. Exit")
    print("=" * 60)


def main():
    ll = O1LinkedList()

    print("\n*** Singly Linked List - Interactive Program ***")

    # Populate list first
    populate_list(ll)

    print("\nMenu accepts positions; list updates are shown after each operation.")

    while True:
        print_menu()
        choice = input("\nEnter your choice (1-5): ").strip()

        if choice == "1":
            print("\n" + "-" * 60)
            print("GET OPERATION")
            print("-" * 60)

            if ll.size == 0:
                print("Error: List is empty! Nothing to get.")
                input("\nPress Enter to continue...")
                continue

            ll.display_with_indices()
            print(f"Current size: {ll.size}")
            print(f"Valid positions: 0 to {ll.size - 1}")

            try:
                position = int(input("Enter position to get: "))

                data = ll.get(position)

                print("\n[RESULT]")
                print(f"Element at position {position}: '{data}'")

            except ValueError:
                print("Error: Please enter a valid number for position.")
            except IndexError as e:
                print(f"Error: {e}")

        elif choice == "2":
            print("\n" + "-" * 60)
            print("INSERT OPERATION")
            print("-" * 60)

            ll.display_with_indices()
            print(f"Current size: {ll.size}")
            print(f"Valid positions: 0 to {ll.size}")

            try:
                position = int(input("Enter position to insert: "))
                data = input("Enter data to insert: ").strip()

                ll.insert(position, data)

                print("\n[RESULT]")
                print(f"Inserted '{data}' at position {position}")
                ll.display_with_indices()
                print(f"New size: {ll.size}")

            except ValueError:
                print("Error: Please enter a valid number for position.")
            except IndexError as e:
                print(f"Error: {e}")

        elif choice == "3":
            print("\n" + "-" * 60)
            print("REMOVE OPERATION")
            print("-" * 60)

            if ll.size == 0:
                print("Error: List is empty! Nothing to remove.")
                input("\nPress Enter to continue...")
                continue

            ll.display_with_indices()
            print(f"Current size: {ll.size}")
            print(f"Valid positions: 0 to {ll.size - 1}")

            try:
                position = int(input("Enter position to remove: "))

                removed_data = ll.remove(position)

                print("\n[RESULT]")
                print(f"Removed '{removed_data}' from position {position}")
                ll.display_with_indices()
                print(f"New size: {ll.size}")

            except ValueError:
                print("Error: Please enter a valid number for position.")
            except IndexError as e:
                print(f"Error: {e}")

        elif choice == "4":
            print("\n" + "-" * 60)
            print("CURRENT LIST")
            print("-" * 60)
            ll.display_with_indices()
            print(f"Size: {ll.size}")

        elif choice == "5":
            print("\nThank you for using the Linked List Tester!")
            print("Exiting program...")
            break

        else:
            print("Error: Invalid choice. Please enter 1-5.")

        input("\nPress Enter to continue...")


if __name__ == "__main__":
    main()
