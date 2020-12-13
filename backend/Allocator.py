class FWsolver():

    # Solves all pair shortest path via Floyd Warshall Algorithm
    def __init__(self, graph):
        self.length = graph.length
        # dist = map(lambda i: map(lambda j: j, i), graph.graph)
        self.dist = [[graph.graph[i][j] for j in range(self.length)] for i in range(self.length)]
        self.next = [[0 for _ in range(self.length)] for __ in range(self.length)]
        for i in range(self.length):
            for j in range(self.length):
                if self.dist[i][j] < INF:
                    self.next[i][j] = j


        for k in range(self.length):

            # pick all vertices as source one by one
            for i in range(self.length):

                # Pick all vertices as destination for the
                # above picked source
                for j in range(self.length):
                    # If vertex k is on the shortest path from
                    # i to j, then update the value of dist[i][j]
                    if (self.dist[i][k] + self.dist[k][j] < self.dist[i][j]):
                        self.dist[i][j] = self.dist[i][k] + self.dist[k][j]
                        self.next[i][j] = self.next[i][k]
        # self.printSolution(self.dist)

    # A utility function to print the solution
    def printSolution(self, dist):
        "Following matrix shows the shortest distances\
    between every pair of vertices"
        print("direct paths")
        for i in range(self.length):
            for j in range(self.length):
                if dist[i][j] == INF:
                    print("&", end=' ')
                else:
                    print(dist[i][j], end=' ')
            print()
        print("next\n", self.next)


    def reconstructPath(self):
        start = int(input("start :"))
        end = int(input("end :"))
        shortest_path = ''
        if (self.dist[start][end] == INF):
            print("not possible")
            return 0
        at = start
        while(at != end):
            shortest_path += str(at) + '->'
            at = self.next[at][end]
        shortest_path += str(end)
        print(shortest_path, self.dist[start][end])
global INF
INF = 99


# create_graph()
# arena_solver = FWsolver(arena)

# arena_solver.reconstructPath()
class Graph2d:
    def __init__(self, length, bidirectional=False):
        INF = 99
        self.bi_dir = bidirectional
        self.length = length
        self.graph = [[INF for i in range(length)] for j in range(length)]
        for i in range(length):
            for j in range(length):
                if i == j:
                    self.graph[i][j] = 0
        # print("Note : Node no starts from 0 not 1")

    def edge(self, i, j, val, bidirectional=False):
        # self.bi_dir is dominant than bidirectional
        if bidirectional or self.bi_dir:
            self.graph[j][i] = val
        self.graph[i][j] = val

    def __str__(self):
        return '2d graph input:\n' + str(self.graph)

class Distance_solver:
    def __init__(self, survivors, saviours, one_time_save=2):
        self.survivors = survivors
        self.saviours = saviours
        self.one_time_save = one_time_save
        self.lenlist = [len(survivors), len(saviours)]
        self.graph = Graph2d(self.lenlist[0] + self.lenlist[1], True)
        self.edge_creator()

    @staticmethod
    def dist_calcy(person1, person2):
        lat1, long1 = person1
        lat2, long2 = person2
        # The math module contains a function named
        # radians which converts from degrees to radians.
        long1 = radians(long1)
        long2 = radians(long2)
        lat1 = radians(lat1)
        lat2 = radians(lat2)

        # Haversine formula
        dlon = long2 - long1
        dlat = lat2 - lat1
        a = sin(dlat / 2) ** 2 + cos(lat1) * cos(lat2) * sin(dlon / 2) ** 2

        c = 2 * asin(sqrt(a))

        # Radius of earth in kilometers. Use 3956 for miles
        r = 6371

        # calculate the result
        return int(c*r)

    def solveit(self):
        print("Solving...")
        seen_savys = []
        seen_users = []
        key = True
        k = 0
        for savy in self.saviours:
            if k < self.lenlist[0] % self.lenlist[1]:
                x = self.lenlist[0]//self.lenlist[1] + 1
            else:
                x = self.lenlist[0]//self.lenlist[1]
            x = min(self.one_time_save, x)
            for j in range(x):
                if len(seen_users) < self.lenlist[0]:
                    if savy.prime_key not in seen_savys:
                        # print(j, len(seen_users), self.lenlist[0])
                        most_suited_survy = self.survivors[0]
                        min_dist = self.graph.graph[self.survivors[0].id_x][self.saviours[0].id_x] + 99*99
                        for survy in self.survivors:
                            if survy.prime_key not in seen_users:
                                if min_dist > self.graph.graph[survy.id_x][savy.id_x]:
                                    most_suited_survy = survy
                                    min_dist = self.graph.graph[survy.id_x][savy.id_x]
                        # print(most_suited_survy.prime_key)
                        if most_suited_survy.prime_key not in seen_users:
                            seen_users.append(most_suited_survy.prime_key)
                            savy.neighbours.append(most_suited_survy)
                            most_suited_survy.neighbours.append(savy)
                            # print(savy.prime_key, most_suited_survy.prime_key)
                else:
                    break
            k += 1

        return self.survivors, self.saviours
    def edge_creator(self):
        for survy in self.survivors:
            for savy in self.saviours:
                self.graph.edge(survy.id_x,
                                savy.id_x,
                                self.dist_calcy(survy.loc_data, savy.loc_data))

import pyrebase
from math import radians, cos, sin, asin, sqrt


firebaseConfig = {
    'apiKey': "AIzaSyDSmHLO48eQvr8i6f-e9Bj0dmGlqlWkLxw",
    'authDomain': "disastermangement-1de94.firebaseapp.com",
    'databaseURL': "https://disastermangement-1de94-default-rtdb.firebaseio.com",
    'projectId': "disastermangement-1de94",
    'storageBucket': "disastermangement-1de94.appspot.com",
    'messagingSenderId': "366752650310",
    'appId': "1:366752650310:web:ab01a77a8f22827320e647",
    'measurementId': "G-Q47TZ3RH7Y"
}

firebase = pyrebase.initialize_app(config=firebaseConfig)

db1 = firebase.database()


class User:
    def __init__(self, id, index, designation, loc_data):
        self.prime_key = id
        self.id_x = index
        self.designation = designation
        self.loc_data = loc_data
        self.neighbours = []

    # def __repr__(self):
    #     return f"{self.prime_key}, {self.designation}, {self.loc_data}"


def distance(lat1, lat2, lon1, lon2):
    # The math module contains a function named
    # radians which converts from degrees to radians.
    lon1 = radians(lon1)
    lon2 = radians(lon2)
    lat1 = radians(lat1)
    lat2 = radians(lat2)

    # Haversine formula
    dlon = lon2 - lon1
    dlat = lat2 - lat1
    a = sin(dlat / 2) ** 2 + cos(lat1) * cos(lat2) * sin(dlon / 2) ** 2

    c = 2 * asin(sqrt(a))

    # Radius of earth in kilometers. Use 3956 for miles
    r = 6371

    # calculate the result
    return (c * r)


# db1.child("Testing").push({'test':1})
x = db1.child('flask_app').get()
ids = x.val().keys()
users = []
i = 0
for prime_key in ids:
    raw = x.val().get(prime_key)
    designation = raw['Designation'][0]
    lat_lng = raw['Location'].split()[1]\
        .strip('(').strip(')').split(',')
    lat_lng = list(map(float, lat_lng))
    # print(lat_lng)
    name = raw['Name']
    user = User(prime_key, i, designation, lat_lng)
    print(user.prime_key, user.id_x, user.designation, user.loc_data)
    users.append(user)
    i += 1


# users = []
# options = ['R', 'S', 'S']
#
# tm_list = [['S', [37, 45]], ['S', [7, 28]], ['R', [5, 41]], ['S', [26, 16]], ['S', [1, 32]], ['R', [37, 25]], ['S', [49, 41]],
#            ['S', [37, 40]],  ['R', [46, 23]], ['S', [50, 33]], ['S', [32, 1]], ['S', [46, 42]], ['S', [1, 18]],
#            ['R', [48, 48]]]
# for i in range(14):
#     user_id = str(i) + '_codex'
#     # k = random.randint(0, 50)
#     # l = random.randint(0, 50)
#     # job = random.choice(options)
#     # tm_list.append([job, [k, l]])
#     users.append(User(user_id, i, tm_list[i][0], tm_list[i][1]))

# print(tm_list)
survivors = []
rescue_staff = []
for user in users:
    if user.designation == 'R':
        rescue_staff.append(user)
    else:
        survivors.append(user)

print('Rescue Staff')
for item in rescue_staff:
    print(item.prime_key)

print('Survivors')
for item in survivors:
    print(item.prime_key)

ds = Distance_solver(survivors, rescue_staff, one_time_save=3)
survivors, rescue_staff = ds.solveit()
# print("output :")
# for item in res_dict.items():
#     print(item.neighbour)

print("solution :")
for item in rescue_staff:
    print(item.prime_key)
    for i in range(len(item.neighbours)):
        print('neighbours :', item.neighbours[i].prime_key)
        db1.child('flask_app').child(item.neighbours[i].prime_key).update({'will_be_recued_by':item.prime_key})
        db1.child('flask_app').child(item.prime_key).\
            update({'to_be_recued_'+str(i+1):item.neighbours[i].prime_key})