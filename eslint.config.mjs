import js from '@eslint/js';
import sonarjs from 'eslint-plugin-sonarjs';
import * as unicorn from 'eslint-plugin-unicorn';
import prettier from 'eslint-plugin-prettier/recommended';
import globals from 'globals';

export default [
    {
        ignores: ['**/*.min.js', '**/build/**'],
    },
    js.configs.recommended,
    sonarjs.configs.recommended,
    unicorn.configs['flat/recommended'],
    prettier,
    {
        languageOptions: {
            globals: {
                ...globals.browser,
                ...globals.es2021,
                jQuery: false,
                $: false,
                _: false,
                up: false,
                fluid: false,
            },
        },
        rules: {
            'no-redeclare': [
                'error',
                {
                    builtinGlobals: false,
                },
            ],
            'sonarjs/cognitive-complexity': 'off',
            'sonarjs/no-duplicate-string': 'off',
            'unicorn/filename-case': 'off',
            'unicorn/no-array-callback-reference': 'off',
            'unicorn/no-for-loop': 'off',
            'unicorn/no-null': 'off',
            'unicorn/no-this-assignment': 'off',
            'unicorn/numeric-separators-style': 'off',
            'unicorn/prefer-module': 'off',
            'unicorn/prefer-spread': 'off',
        },
    },
];
