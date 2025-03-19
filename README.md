# Tema 3 Poo
## Szabo Cristina-Andreea 324CA
Pentru partea a 3-a a proiectului am implementat mai multe functii:
1. wrapped - functia care furnizeaza statisticile pentru un user normal, host sau artist.
Pentru aceasta functie am pus pentru fiecare user melodiile pe care le asculta intr-un hashmap.
Am pus mai intai melodiile la care da load direct userul, apoi am adaugat melodiile ascultate odata cu
trecerea timpului in cazul in care se asculta un album sau un playlist. Pentru a face acest lucru
am verificat mai intai in functia load daca melodia e deja in hashmap si am crescut contorul, iar
pentru cazul de playlist/album am adaugat melodiile noi in functia simulatePlayer, cand se trece
la o noua melodie din colectia audio. Apoi am creat functia wrapped, unde calculez statisticile
pentru userul cerut. Verific tipul de user, apoi in functie de acesta afisez statisticile.
Pentru un user normal, am iterat prin hashmap ul de melodii ascultate de acesta si am calculat
numarul de melodii din fiecare gen, de la fiecare artist, topul cantecelor, al episoadelor.
Am adaugat pentru fiecare categorie melodiile in functie de conditia ceruta in hashmap-uri
diferite, pe care apoi le am sortat si am pastrat doar primele 5 elemenete din fiecare.
Pentru artisti am facut acelasi lucru, doar ca pentru topul cantecelor, al albumelor
si topul fanilor, pe care il creez punand intr-un hashmap fiecare user care are in lista
cantecelor ascultate artistul cautat si luand descrescator in functie de numarul de piese ascultate.
Pentru host am verificat daca un user are in player un episod dintr-un podcast, apoi am luat host-ul
episodului respectiv si daca se potrivea cu host-ul cautat adaugam intr-un hashmap user-ul pentru a
face topul fanilor. De asemenea, am facut topul episoadelor si numarul de ascultatori luand size-ul
listei fanilor.
2. Monetizare - am incercat sa pun toate melodiile ascultate de userii premium intr-o lista pentru fiecare user, apoi sa calculez la final sau daca se da cancel dupa formula data banii primiti de fiecare
artist
3. buyMerch - am verificat daca merch-ul cerut exista in lista de merch artistului cerut si apoi
am adaugat noul merch in lista user-ului curent, am crescut venitul venit din merch al artistului
respectiv si am adaugat artistul in lista din care afisez la endprogram, in cazul in care nimeni nu a
ascultat ceva de la el pana atunci.
4. seeMecrh - am afisat numele merch-ul din lista user-ului curent iterand prin lista de merch a user-ului
5. buyPremium si cancelPremium, doua functii care nu mi-au iesit cum trebuia, pentru prima am
schimbat statusul userului in premium daca nu era deja, iar la a doua am calculat venitul dat fiecarui
artist si apoi am schimbat statusul utilizatorului in user normal daca era premium inainte
6. Subscribe - am adaugat user-ul pe lista de subscriberi a artistului respectiv daca nu era deja,
altfel l-am eliminat si am afisat mesaje corespunzatoare
7. getNotifications - am adaugat cate o notificare in lista de notificari a fiecarui user care
se afla in lista de subscriberi a artistului care efectueaza o noua actiune. De exemplu, la addAlbum,
am verificat ce artist adauga noul album, si am adaugat o notificare pentru fiecare user din lista lui
de subscriberi, la fel si la addEvent
8. Pentru recomandari, am folosit functia update, unde am verificat tipul de recomandare ceruta si am
verificat pe cazuri. Pentru fans_playlist, am creat un playlist nou cu numele cerut si l-am adaugat la recomandari, la fel si pentru random_playlist, iar pentru random_song am luat cantecul din player, am verificat daca au trecut mai mult de 30 de secunde din el, am calculat seed-ul si am luat indexul
random in functie de acesta, asta dupa ce am pus doar cantecele cu acelasi genre intr-o lista,
apoi am selectat cantecul din lista dupa indexul obtinut.
La comanda loadRecommendations, am luat ultima recomandare daca era de tip song si am pus cantecul in
player, apoi l-am adaugat la hashmap-ul cantecelor ascultate de user
9. Page Navigation - contine nextPage si prevPage, dar se leaga si de changePage.
Adaug la o lista de pagini initial pagina home, apoi la fiecare change page noua pagina accesata.
Retin indexul la care ma aflu in aceasta lista. Cand am nextPage, verific daca indexul este maxim,
atunci nu se poate trece la pagina urmatoare, altfel maresc indexul cu 1, iar la prevPage scad indexul
cu 1 pentru a accesa pagina anterioara. La o noua comanda changePage, daca indexul nu este cat size-1,
sterg paginile care se afla dupa index si adaug noua pagina dupa pagina cu indexul curent, apoi cresc
indexul cu 1

Am folosit scheletul de la tema 2, chatgpt pentru sortarile de liste, singleton pentru ca Admin sa aiba o singura instanta care poate fi accesata global si Factory pentru a crea mai usor pagini de oricare tip, fara sa conteze tipul lor.
