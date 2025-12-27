structurizr.ui.QuickNavigation = class QuickNavigation {

    #modal = $('#quickFindModal');
    #filterTextBox = $('#quickFindModalFilter');
    #navigationLinksClass = '.quickNavigationItem';

    #selectedIndex = -1;
    #maxItems = 20;
    #enabled = true;

    #onOpenCallback;
    #onCloseCallback;

    constructor() {
        const self = this;

        $(document).keydown(function (e) {
            const spaceKeyCode = 32;

            if (e.which === spaceKeyCode && self.#isEnabled() === true && !self.isOpen() && (e.target.tagName.toLowerCase() !== 'input' && e.target.tagName.toLowerCase() !== 'textarea')) {
                e.preventDefault();
                self.#open();
            }
        });

        this.#modal.on('shown.bs.modal', function () {
            if (self.#onOpenCallback) {
                self.#onOpenCallback();
            }

            self.#filterTextBox.focus();
        });

        this.#modal.on('hidden.bs.modal', function () {
            if (self.#onCloseCallback) {
                self.#onCloseCallback();
            }
        });

        this.#filterTextBox.on('keydown', function (e) {
            const upArrowKeyCode = 38;
            const downArrowKeyCode = 40;
            const enterKeyCode = 13;

            if (e.which === downArrowKeyCode) {
                self.#selectNextItem();
                e.preventDefault();
            } else if (e.which === upArrowKeyCode) {
                self.#selectPreviousItem();
                e.preventDefault();
            } else if (e.which === enterKeyCode) {
                e.preventDefault();

                const navigationLinks = $(self.#navigationLinksClass).not('.hidden');
                if (navigationLinks.length > 0) {
                    navigationLinks[self.#selectedIndex].click();
                }
            }
        });

        this.#filterTextBox.on('change keyup', function (e) {
            const upArrowKeyCode = 38;
            const downArrowKeyCode = 40;
            const enterKeyCode = 13;

            if (e.which === downArrowKeyCode) {
                e.preventDefault();
            } else if (e.which === upArrowKeyCode) {
                e.preventDefault();
            } else if (e.which === enterKeyCode) {
                e.preventDefault();
            } else if (e.which !== undefined) {
                self.#filterItems();
            }
        });
    }

    #filterItems() {
        const self = this;
        const filter = this.#filterTextBox.val().trim().toLowerCase();
        var count = 0;

        const navigationLinks = $(this.#navigationLinksClass);
        navigationLinks.each(function(index) {
            if ($(this).text().toLowerCase().indexOf(filter) > -1) {
                count++;
                if (count <= self.#maxItems) {
                    $(this).removeClass('hidden');
                    $(this).removeClass('selected');
                } else {
                    $(this).addClass('hidden');
                    $(this).removeClass('selected');
                }
            } else {
                $(this).addClass('hidden');
                $(this).removeClass('selected');
            }
        });

        this.#selectItem(0);
    }

    #selectItem(index) {
        const items = $(this.#navigationLinksClass).not('.hidden');
        if (items.length > 0) {
            this.#selectedIndex = index;
            items.removeClass('selected');
            items.eq(this.#selectedIndex).addClass('selected');
        }
    }

    #selectNextItem() {
        const items = $(this.#navigationLinksClass).not('.hidden');
        if (items.length > (this.#selectedIndex + 1)) {
            this.#selectItem(this.#selectedIndex + 1);
        }
    }

    #selectPreviousItem() {
        const items = $(this.#navigationLinksClass).not('.hidden');
        if (items.length > 0 && this.#selectedIndex > 0) {
            this.#selectItem(this.#selectedIndex - 1);
        }
    }

    #open() {
        if (this.#selectedIndex === -1) {
            this.#filterTextBox.val('');

            this.#filterItems();
        }

        this.#modal.modal('show');
    }

    onOpen(callback) {
        this.#onOpenCallback = callback;
    }

    isOpen() {
        return this.#modal.is(':visible');
    }

    close() {
        this.#modal.modal('hide');
    }

    onClose(callback) {
        this.#onCloseCallback = callback;
    }

    addItem(display, link) {
        this.addHandler(display, function() {
            window.location.href = link;
        });
    }

    addHandler(display, handler) {
        const quickFindModalItems = $('#quickFindModalItems');
        const div = document.createElement('div');
        div.className = 'quickNavigationItem hidden';
        div.innerHTML = display;

        quickFindModalItems.append(div);

        div.onclick = function() {
            handler();
            quickNavigation.close();
        };
    }

    #isEnabled() {
        return this.#enabled;
    }

    disable() {
        this.#enabled = false;
    }

    enable() {
        this.#enabled = true;
    }

    clear() {
        const quickFindModalItems = $('#quickFindModalItems');
        quickFindModalItems.empty();
        this.#selectedIndex = -1;
    };
}

const quickNavigation = new structurizr.ui.QuickNavigation();