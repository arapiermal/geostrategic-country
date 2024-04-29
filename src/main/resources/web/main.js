let settings = []; // maybe dictionary
function saveSettingsToLocalStorage() {
    localStorage.setItem('settings', JSON.stringify(settings));
}

function loadSettingsFromLocalStorage() {
    const savedSettings = localStorage.getItem('settings');
    if (savedSettings) {
        settings = JSON.parse(savedSettings);
        loadSettings();
    }
}
class Shortcut {
    //function/url
    constructor(name, action, icon) {
        this.name = name;
        this.action = action;
        this.icon = icon;
    }
}

function loadShortcuts() {
    return fetch('shortcuts.json')
        .then(response => response.json())
        .then(data => {
            let shortcuts = data.map(item => new Shortcut(item.name, item.action, item.icon));

            showShortcuts(shortcuts);
        })
        .catch(error => {
            console.error('Error:', error);
        });
}

function showShortcuts(shortcuts) {
    const field = document.getElementById('field');
    const fragment = document.createDocumentFragment();

    for (let i = 0; i < shortcuts.length; i++) {
        const container = document.createElement('div');
        container.classList.add('icon');
        const a = document.createElement('a');
        a.href = shortcuts[i].action;
        const icon = document.createElement('img');
        icon.alt = shortcuts[i].name;
        icon.src = 'img/' + shortcuts[i].icon;
        a.appendChild(icon);
        const name = document.createElement('p');
        name.textContent = shortcuts[i].name;
        a.appendChild(name);
        container.appendChild(a);
        fragment.appendChild(container);
    }
    /*
    shortcuts.forEach(shortcut => {
        const container = document.createElement('div');
        container.classList.add('icon');
        const a = document.createElement('a');
        a.href = shortcut.action;
        const icon = document.createElement('img');
        icon.alt = shortcut.name;
        icon.src = 'img/' + shortcut.icon; 
        a.appendChild(icon);
        const name = document.createElement('p');
        name.textContent = shortcut.name;
        a.appendChild(name);
        container.appendChild(a);
        fragment.appendChild(container);
    });*/

    field.appendChild(fragment);

}
loadShortcuts();

///////////////////////////////////////////////
///////////////////////////////////////////////

const icons = document.querySelectorAll('.icon');
const iconContextMenu = document.getElementById('context-menu');
const desktopContextMenu = document.getElementById('general-context-menu');

function showIconContextMenu(event) {
    event.preventDefault();
    iconContextMenu.style.display = 'block';
    iconContextMenu.style.left = event.clientX + 'px';
    iconContextMenu.style.top = event.clientY + 'px';
    desktopContextMenu.style.display = 'none';
    event.stopPropagation();
}

function showDesktopContextMenu(event) {
    event.preventDefault();
    desktopContextMenu.style.display = 'block';
    desktopContextMenu.style.left = event.clientX + 'px';
    desktopContextMenu.style.top = event.clientY + 'px';
    iconContextMenu.style.display = 'none';
}

function hideContextMenus() {
    iconContextMenu.style.display = 'none';
    desktopContextMenu.style.display = 'none';
}

icons.forEach(icon => {
    icon.addEventListener('contextmenu', showIconContextMenu);
});
const desktop = document.getElementById('desktop');
desktop.addEventListener('contextmenu', showDesktopContextMenu);
window.addEventListener('click', hideContextMenus);
function changeBackgroundColor() {
    const colorPicker = document.createElement('input');
    colorPicker.type = 'color';
    //set position fix
    colorPicker.addEventListener('input', function (event) {
        document.body.style.backgroundColor = event.target.value;
    });
    colorPicker.click();
}
const changeBgColorItem = document.querySelector('.change-bg-color');
changeBgColorItem.addEventListener('click', changeBackgroundColor);

//on clicked anywhere outside down bar

var isStartShowing = false;
const startMenu = document.getElementById('start-menu');
function showstartmenu() {
    isStartShowing = !isStartShowing;
    if (isStartShowing) {
        startMenu.style.display = 'block';
    } else {
        startMenu.style.display = 'none';
    }
}


const startButton = document.getElementById('start');
startButton.addEventListener('click', showstartmenu);



document.addEventListener("DOMContentLoaded", function () {
    const field = document.getElementById("field");
    const textWindow = document.getElementById("textWindow");
    //sth to show name to save to
    const textFileName = document.getElementById("textFileName");
    const textArea = document.getElementById("textArea");
    const saveButton = document.getElementById("saveButton");
    const closeButton = document.getElementById("closeButton");
    const deleteTxtFile = (fileName) => {

    }
    const onClickTxtFileIcon = (fileName) => {
        textWindow.style.display = "block";
        textFileName.value = fileName;
        const savedText = localStorage.getItem(`${fileName}.txt`);
        if (savedText) {
            textArea.value = savedText;
        } else {
            textArea.value = '';
        }
    };
    const renameTxtFile = (fileName) => {
        //const divTxt = document.querySelector(`#${fileName}.txt`);
        const divTxt = document.getElementById(`${fileName}.txt`);
        //fragment?
        const renameDiv = document.createElement('div');

        const renameInput = document.createElement('input');
        renameInput.value = fileName;
        renameDiv.appendChild(renameInput);
        const renameButton = document.createElement('button');
        renameButton.textContent = 'OK';
        renameButton.onclick = () => {
            divTxt.removeChild(renameDiv);
            renameLocalStorageItem(fileName, renameInput.value);
        };
        renameDiv.appendChild(renameButton);

        divTxt.appendChild(renameDiv);

        //add .txt
    };
    const renameLocalStorageItem = (oldName, newName) => {
        const oldFullName = `${oldName}.txt`;
        const newFullName = `${newName}.txt`;
        const text = localStorage.getItem(oldFullName);
        localStorage.removeItem(oldFullName);
        localStorage.setItem(newFullName, text);
        const txtFiles = localStorage.getItem('txtFiles');
        const newTxtFiles = txtFiles.replace(oldFullName, newFullName);
        localStorage.setItem('txtFiles', newTxtFiles);

    }
    const makeTxtFileIcon = (fileName) => {
        const fragment = document.createDocumentFragment();
        const container = document.createElement('div');
        container.classList.add('icon');
        container.classList.add('txtFileIcon');
        container.id = `${fileName}.txt`;
        container.onclick = () => onClickTxtFileIcon(fileName);
        const icon = document.createElement('img');
        icon.alt = fileName;
        icon.src = 'img/edit.svg';
        container.appendChild(icon);
        const name = document.createElement('p');
        name.textContent = `${fileName}.txt`;
        //name unreachable?
        //Uncaught TypeError: Cannot read properties of null (reading 'appendChild')
        container.ondblclick = () => renameTxtFile(fileName);
        container.appendChild(name);
        fragment.appendChild(container);
        return fragment;
    };
    const showAllTxtFileIcons = () => {
        const txtFiles = localStorage.getItem("txtFiles");
        if (txtFiles) {
            const textFileNames = txtFiles.split(',');
            if (textFileNames) {
                for (const fileName of textFileNames) {
                    const fragment = makeTxtFileIcon(fileName);
                    field.appendChild(fragment);
                }
            }
        }
        else {
            localStorage.setItem("txtFiles", "");
        }
    };
    showAllTxtFileIcons();

    saveButton.addEventListener("click", () => {
        const fileName = textFileName.value;
        localStorage.setItem(`${fileName}.txt`, textArea.value);
        const txtFiles = localStorage.getItem('txtFiles').split(',');
        console.log(txtFiles);
        if (txtFiles.indexOf(fileName) < 0) {
            txtFiles.push(fileName);
            localStorage.setItem('txtFiles', txtFiles.toString());
            const fragment = makeTxtFileIcon(fileName);
            field.appendChild(fragment);
        }
    });
    // txtFileName.value on changed => save/saveas
    closeButton.addEventListener("click", () => {
        textWindow.style.display = "none";
    });
    document.querySelector("#padnote").addEventListener("click", function () {
        textWindow.style.display = "block";
        textFileName.value = '';
        textArea.value = '';
    });
    const URLparams = new URLSearchParams(document.location.search);
    const gameId = URLparams.get('gameId');
    const newsLink = document.getElementById('newsLink');
    newsLink.href += gameId;
});
