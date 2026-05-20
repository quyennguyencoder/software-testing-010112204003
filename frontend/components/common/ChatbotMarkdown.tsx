'use client';

import React from 'react';
import { cn } from '@/lib/utils';

interface ChatbotMarkdownProps {
  content: string;
  className?: string;
}

/**
 * Component render Markdown cho Chatbot response
 * Hỗ trợ:
 * - **bold** → <strong>
 * - *italic* → <em>
 * - `code/highlight` → <code>
 * - ~~strikethrough~~ → <del>
 * - • bullet points
 * - Emojis giữ nguyên
 * - Line breaks
 */
export const ChatbotMarkdown: React.FC<ChatbotMarkdownProps> = ({
  content,
  className,
}) => {
  // Parse markdown và render
  const renderMarkdown = (text: string): React.ReactNode[] => {
    const lines = text.split('\n');
    
    return lines.map((line, lineIndex) => {
      // Parse inline markdown
      const parsedLine = parseInlineMarkdown(line);
      
      // Check if this is a bullet point line
      const isBullet = line.trim().startsWith('•') || line.trim().startsWith('-');
      const isNumbered = /^\d+\./.test(line.trim());
      
      if (isBullet) {
        return (
          <div key={lineIndex} className="flex items-start gap-2 my-1 ml-2">
            <span className="text-primary mt-0.5">•</span>
            <span>{parsedLine}</span>
          </div>
        );
      }
      
      if (isNumbered) {
        return (
          <div key={lineIndex} className="flex items-start gap-2 my-1 ml-2">
            <span className="text-primary font-semibold">{line.match(/^\d+\./)?.[0]}</span>
            <span>{parseInlineMarkdown(line.replace(/^\d+\.\s*/, ''))}</span>
          </div>
        );
      }
      
      // Empty line = paragraph break
      if (line.trim() === '') {
        return <div key={lineIndex} className="h-2" />;
      }
      
      // Heading-like lines (với emoji ở đầu)
      const hasEmoji = /^[🏆🥈🥉🌟🔥✨💾💿🔋📱💰🏷️🔍💡📌📦🚀👆🎯⚠️═]+/.test(line.trim());
      if (hasEmoji && line.includes('**')) {
        return (
          <div key={lineIndex} className="font-medium text-foreground my-1">
            {parsedLine}
          </div>
        );
      }
      
      // Separator lines
      if (line.includes('═══')) {
        return <div key={lineIndex} className="border-t border-border my-2" />;
      }
      
      return (
        <div key={lineIndex} className="my-0.5">
          {parsedLine}
        </div>
      );
    });
  };

  // Parse inline markdown (bold, italic, code, strikethrough)
  const parseInlineMarkdown = (text: string): React.ReactNode => {
    // Remove bullet prefix if present
    let processedText = text.replace(/^[•\-]\s*/, '');
    
    // Split by markdown patterns
    const parts: React.ReactNode[] = [];
    let remaining = processedText;
    let key = 0;

    // Regex patterns
    const patterns = [
      // **bold**
      { regex: /\*\*([^*]+)\*\*/g, render: (match: string) => (
        <strong key={key++} className="font-semibold text-foreground">{match}</strong>
      )},
      // *italic*
      { regex: /(?<!\*)\*([^*]+)\*(?!\*)/g, render: (match: string) => (
        <em key={key++} className="italic text-muted-foreground">{match}</em>
      )},
      // `code/highlight`
      { regex: /`([^`]+)`/g, render: (match: string) => (
        <code key={key++} className="px-1.5 py-0.5 rounded bg-primary/10 text-primary text-xs font-medium">
          {match}
        </code>
      )},
      // ~~strikethrough~~
      { regex: /~~([^~]+)~~/g, render: (match: string) => (
        <del key={key++} className="text-muted-foreground line-through">{match}</del>
      )},
    ];

    // Process text with all patterns
    let processedNode = processedText;
    
    // Bold: **text**
    processedNode = processedNode.replace(/\*\*([^*]+)\*\*/g, '‹BOLD›$1‹/BOLD›');
    // Italic: *text* (not double)
    processedNode = processedNode.replace(/(?<!\*)(?<!‹\/BOLD)\*([^*‹]+)\*(?!\*)/g, '‹ITALIC›$1‹/ITALIC›');
    // Code: `text`
    processedNode = processedNode.replace(/`([^`]+)`/g, '‹CODE›$1‹/CODE›');
    // Strikethrough: ~~text~~
    processedNode = processedNode.replace(/~~([^~]+)~~/g, '‹STRIKE›$1‹/STRIKE›');
    
    // Split and render
    const segments = processedNode.split(/(‹[A-Z]+›[^‹]+‹\/[A-Z]+›)/g);
    
    return segments.map((segment, idx) => {
      if (segment.startsWith('‹BOLD›')) {
        const content = segment.replace('‹BOLD›', '').replace('‹/BOLD›', '');
        return <strong key={idx} className="font-semibold text-foreground">{content}</strong>;
      }
      if (segment.startsWith('‹ITALIC›')) {
        const content = segment.replace('‹ITALIC›', '').replace('‹/ITALIC›', '');
        return <em key={idx} className="italic text-muted-foreground">{content}</em>;
      }
      if (segment.startsWith('‹CODE›')) {
        const content = segment.replace('‹CODE›', '').replace('‹/CODE›', '');
        return (
          <code key={idx} className="px-1.5 py-0.5 rounded-md bg-primary/10 text-primary text-xs font-mono font-medium">
            {content}
          </code>
        );
      }
      if (segment.startsWith('‹STRIKE›')) {
        const content = segment.replace('‹STRIKE›', '').replace('‹/STRIKE›', '');
        return <del key={idx} className="text-muted-foreground line-through">{content}</del>;
      }
      return segment;
    });
  };

  return (
    <div className={cn('text-sm leading-relaxed', className)}>
      {renderMarkdown(content)}
    </div>
  );
};

export default ChatbotMarkdown;
